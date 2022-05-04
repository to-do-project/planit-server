package com.planz.planit.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.DeviceTokenRepository;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.user.UserCharacterColor;
import com.planz.planit.src.domain.user.dto.LoginResDTO;
import com.planz.planit.src.service.DeviceTokenService;
import com.planz.planit.src.service.HttpResponseService;
import com.planz.planit.config.security.auth.PrincipalDetails;
import com.planz.planit.src.domain.user.dto.LoginReqDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.service.JwtTokenService;
import com.planz.planit.utils.ValidationRegex;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.planz.planit.config.BaseResponseStatus.*;

/*
body에 email과 password를 담아 POST /login 요청을 하면,
스프링 시큐리티의 UsernamePasswordAuthenticationFilter가 동작함.
 */
@Log4j2
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String ACCESS_TOKEN_HEADER_NAME = "Jwt-Access-Token";
    private static final String REFRESH_TOKEN_HEADER_NAME = "Jwt-Refresh-Token";

    // AuthenticationManger를 통해 로그인 진행
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final HttpResponseService httpResponseService;
    private final DeviceTokenService deviceTokenService;
    private final DeviceTokenRepository deviceTokenRepository;

    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, ObjectMapper objectMapper, HttpResponseService httpResponseService, DeviceTokenService deviceTokenService, DeviceTokenRepository deviceTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
        this.httpResponseService = httpResponseService;
        this.deviceTokenService = deviceTokenService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    // /login 요청이 오면, 로그인 시도를 위해서 자동으로 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도 중");

        try {
            // 1. body에 담겨있는 JSON을 파싱해서, email, password, deviceToken 얻기
            LoginReqDTO loginReqDTO = objectMapper.readValue(request.getInputStream(), LoginReqDTO.class);

            if (validateLoginReqDTO(response, loginReqDTO) == false){
                log.error("로그인 시도 중, request validation 실패");
                return null;
            }

            log.info("로그인 시도 사용자 => email : " + loginReqDTO.getEmail());

            // 2. UsernamePasswordAuthenticationToken 생성하기
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginReqDTO.getEmail(), loginReqDTO.getPassword());
            // AuthenticationManger로 로그인 시도 => PrincipalDetailsService의 loadUserByUsername() 함수가 자동으로 실행됨
            // 로그인 시도가 정상적으로 완료되면 Authentication 객체 반환, 비정상적으로 완료되면 AuthenticationException 발생
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            log.info("로그인 성공 => 입력으로 받은 Device Token을 User에 저장중");
            ((PrincipalDetails) authentication.getPrincipal()).getUser().setDeviceToken(loginReqDTO.getDeviceToken());

            // authentication 객체가 session 영역에 저장을 해야되야하고, 그 방법이 return!
            return authentication;
        } catch (IOException e) {
            try {
                log.error("로그인 시도 중에 IOException 발생");
                httpResponseService.errorRespond(response, IO_EXCEPTION);
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (AuthenticationException e) {
            try {
                log.error("로그인 시도 중에 AuthenticationException 발생");
                httpResponseService.errorRespond(response, INVALID_ID_OR_PWD);
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        return null;
    }



    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        log.info("로그인 완료, 토큰 반환 중");

        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        User loginUser = principalDetails.getUser();

        // request로 받은 device token을 DB에 저장하기!!!!! => 수정 필요!!!
        try {
            deviceTokenService.createDeviceToken(loginUser.getUserId(), new DeviceTokenReqDTO(loginUser.getDeviceToken()));
        } catch (BaseException e) {
            log.error("로그인 후 DB에 디바이스 토큰 저장 도중에 에러 발생");
            httpResponseService.errorRespond(response, e.getStatus());
            e.printStackTrace();
        }

        // access token, refresh token 생성해서 헤더에 담기
        DeviceToken findDeviceToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(loginUser.getUserId(), loginUser.getDeviceToken());
        String jwtRefreshToken = jwtTokenService.createRefreshToken(findDeviceToken.getDeviceTokenId().toString());
        String jwtAccessToken = jwtTokenService.createAccessToken(loginUser.getUserId().toString(), loginUser.getRole());

        response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + jwtAccessToken);
        response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + jwtRefreshToken);


        // 성공메시지 리턴하기
        LoginResDTO loginResDTO = LoginResDTO.builder()
                .userId(loginUser.getUserId())
                .planetId(loginUser.getPlanet().getPlanetId())
                .email(loginUser.getEmail())
                .nickname(loginUser.getNickname())
                .characterColor(loginUser.getCharacterColor().name())
                .profileColor(loginUser.getProfileColor().name())
                .point(loginUser.getPoint())
                .missionStatus(loginUser.getMissionStatus())
                .deviceToken(loginUser.getDeviceToken())
                .build();

        httpResponseService.successRespond(response, loginResDTO);

    }


    private boolean validateLoginReqDTO(HttpServletResponse response, LoginReqDTO loginReqDTO) throws IOException {

        if(loginReqDTO.getEmail()==null || loginReqDTO.getPassword()==null || loginReqDTO.getDeviceToken()==null){
            log.error("email, password, deviceToken을 모두 입력해주세요.");
            httpResponseService.errorRespond(response, NOT_EXIST_LOGIN_REQ_DTO);
            return false;
        }

        if(ValidationRegex.isRegexEmail(loginReqDTO.getEmail()) == false){
            log.error("이메일 형식이 올바르지 않습니다.");
            httpResponseService.errorRespond(response, INVALID_EMAIL_FORM);
            return false;
        }

        if(ValidationRegex.isRegexPassword(loginReqDTO.getPassword()) == false){
            log.error("비밀번호 형식이 올바르지 않습니다. (영문+숫자 6~15자)");
            httpResponseService.errorRespond(response, INVALID_PWD_FORM);
            return false;
        }

        return true;
    }
}
