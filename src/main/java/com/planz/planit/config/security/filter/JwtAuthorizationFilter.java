package com.planz.planit.config.security.filter;

import com.planz.planit.src.service.HttpResponseService;
import com.planz.planit.config.security.auth.PrincipalDetails;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRepository;
import com.planz.planit.src.service.JwtTokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String USER_ID_HEADER_NAME = "User-Id";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final HttpResponseService httpResponseService;

    @Autowired
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserRepository userRepository, HttpResponseService httpResponseService) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.httpResponseService = httpResponseService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("요청 URI " + request.getRequestURI());

        // 회원가입 요청인 경우
        if (request.getRequestURI().startsWith("/join")) {
            chain.doFilter(request, response);
            return;
        }

        // 인증이 필요한 요청인 경우
        log.info("인증이나 권한이 필요한 주고사 요청이 됨 => " + request.getRequestURI());

        // 헤더에서 userId 받아오기
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        // 헤더에서 jwtAccessToken 받아오기
        String jwtAccessToken = jwtTokenService.getAccessToken(request);


        if (checkUserIdInHeader(response, userId) && checkJwtAccessTokenInHeader(response, jwtAccessToken)) {
            User userEntity = findUser(response, userId, jwtAccessToken);

            if (userEntity != null) {
                PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
                log.info("principalDetails's username : " + principalDetails.getUsername());

                // JWT 토큰 서명을 통해서, 서명이 정상이면 Authentication 객체를 만들어준다.
                Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

                // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                log.info("로그인한 사용자의 Principal- " + authentication.getPrincipal());
                log.info("로그인한 사용자의 Authorities- " + authentication.getAuthorities());
                chain.doFilter(request, response);
            }
        }


    }

    // header에서 userId를 뽑아, 유효한 userId인지 검사
    private boolean checkUserIdInHeader(HttpServletResponse response, String userId) throws IOException {

        if (userId == null) {
            log.error("userId를 헤더에 입력해주세요");
            httpResponseService.errorRespond(response, NOT_EXIST_USER_ID_IN_HEADER);
            return false;
        }

        try {
            Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("유효하지 않은 userId입니다. 숫자형태로 입력해주세요.");
            httpResponseService.errorRespond(response, INVALID_USER_ID);
            return false;
        }

        return true;

    }


    // header에서 jwtAccessToken을 뽑아, 유효한 jwtAccessToken인지 검사
    private boolean checkJwtAccessTokenInHeader(HttpServletResponse response, String jwtAccessToken) throws IOException {
        if (jwtAccessToken == null) {
            log.error("access token을 헤더에 입력해주세요.");
            httpResponseService.errorRespond(response, NOT_EXIST_ACCESS_TOKEN_IN_HEADER);
            return false;
        }

        if (jwtTokenService.validateAccessToken(jwtAccessToken) == false) {
            log.error("유효하지 않은 access token입니다.");
            httpResponseService.errorRespond(response, INVALID_ACCESS_TOKEN);
            return false;
        }

        log.info("jwtAccessToken : " + jwtAccessToken);
        return true;
    }


    private User findUser(HttpServletResponse response, String userId, String jwtAccessToken) throws IOException, NumberFormatException {

        String userPk = jwtTokenService.getUserPkInAccessToken(jwtAccessToken);

        if (userPk == null) {
            log.error("access token에 subject가 존재하지 않습니다.");
            httpResponseService.errorRespond(response, NOT_EXIST_ACCESS_TOKEN_SUBJECT);
            return null;
        }

        if (!userPk.equals(userId)) {
            log.error("accessToken의 userId와 header의 userId가 일치하지 않습니다.");
            httpResponseService.errorRespond(response, NOT_EQUAL_USER_ID);
            return null;
        }

        User userEntity = userRepository.findByuserId(Long.valueOf(userPk)).orElse(null);

        if (userEntity == null) {
            log.error("존재하지 않는 사용자입니다.");
            httpResponseService.errorRespond(response, NOT_EXIST_USER);
            return null;
        }

        return userEntity;
    }

}
