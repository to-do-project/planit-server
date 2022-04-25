package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.mail.MailDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetColor;
import com.planz.planit.src.domain.planet.PlanetRepository;
import com.planz.planit.src.domain.user.*;
import com.planz.planit.src.domain.user.dto.JoinReqDTO;
import com.planz.planit.src.domain.user.dto.LoginResDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Random;

import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
@Service
public class UserService {

    @Value("${jwt.access-token-header-name}")
    private String ACCESS_TOKEN_HEADER_NAME;

    @Value("${jwt.refresh-token-header-name}")
    private String REFRESH_TOKEN_HEADER_NAME;

    private final UserRepository userRepository;
    private final PlanetRepository planetRepository;
    private final JwtTokenService jwtTokenService;
    private final MailService mailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PlanetRepository planetRepository, JwtTokenService jwtTokenService, MailService mailService, RedisService redisService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.planetRepository = planetRepository;
        this.jwtTokenService = jwtTokenService;
        this.mailService = mailService;
        this.redisService = redisService;
        this.passwordEncoder = passwordEncoder;
    }


    public LoginResDTO join(JoinReqDTO reqDTO, HttpServletResponse response) throws BaseException {

        // 닉네임 존재 여부 확인
        if (!isEmptyNickname(reqDTO.getNickname())) {
            throw new BaseException(ALREADY_EXIST_NICKNAME);
        }

        // 이메일 존재 여부 확인
        if (!isEmptyEmail(reqDTO.getEmail())) {
            throw new BaseException(ALREADY_EXIST_EMAIL);
        }

        try {

            // User 저장
            User userEntity = User.builder()
                    .email(reqDTO.getEmail())
                    .password(passwordEncoder.encode(reqDTO.getPassword()))
                    .nickname(reqDTO.getNickname())
                    .characterColor(UserCharacterColor.WHITE)
                    .profileColor(UserProfileColor.WHITE)
                    .point(0)
                    .missionStatus(1)
                    .userStatus(UserStatus.VALID)
                    .role(UserRole.ROLE_USER)
                    .deviceToken(reqDTO.getDeviceToken())
                    .build();

            userRepository.save(userEntity);

            // request로 받은 device token을 DB에 저장하기!!!!! => 수정 필요!!!
            log.info("userId : " + userEntity.getUserId());
            log.info("deviceToken : " + userEntity.getDeviceToken());


            // Planet 저장
            Planet planetEntity = Planet.builder()
                    .user(userEntity)
                    .level(0)
                    .exp(0)
                    .color(PlanetColor.valueOf(reqDTO.getPlanetColor()))
                    .build();

            planetRepository.save(planetEntity);


            // access token, refresh token 생성해서 헤더에 담기
            String jwtRefreshToken = jwtTokenService.createRefreshToken(userEntity.getUserId().toString());
            String jwtAccessToken = jwtTokenService.createAccessToken(userEntity.getUserId().toString(), userEntity.getRole());

            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + jwtAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + jwtRefreshToken);


            return LoginResDTO.builder()
                    .userId(userEntity.getUserId())
                    .planetId(planetEntity.getPlanetId())
                    .email(userEntity.getEmail())
                    .nickname(userEntity.getNickname())
                    .characterColor(userEntity.getCharacterColor().name())
                    .profileColor(userEntity.getProfileColor().name())
                    .point(userEntity.getPoint())
                    .missionStatus(userEntity.getMissionStatus())
                    .deviceToken(userEntity.getDeviceToken())
                    .build();
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }

    }

    public boolean isEmptyEmail(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    public boolean isEmptyNickname(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }


    // 인증번호 발송
    public void createAuthNum(String email) throws BaseException {

        // 이메일 존재 여부 확인
        if (!isEmptyEmail(email)) {
            throw new BaseException(ALREADY_EXIST_EMAIL);
        }

        try {
            String myRandomNum = randomNumber(email);
            // 6자리 인증번호 이메일 발송
            MailDTO mailDTO = MailDTO.builder()
                    .address(email)
                    .title("[PLAN-IT] 인증번호 안내")
                    .content("입력하실 6자리 인증번호는 " + myRandomNum + " 입니다.")
                    .build();

            mailService.mailSend(mailDTO);

            // 인증번호 Redis에 저장
            redisService.setEmailAuthNumInRedis(email, myRandomNum);

        } catch (BaseException e) {
            throw e;
        }
    }

    // 6자리 인증번호 생성
    public String randomNumber(String email) {
        String authNumber = "";
        Random random = new Random(System.currentTimeMillis() + email.length());

        int i, randomNum;
        for (i = 0; i < 6; i++) {
            randomNum = random.nextInt(10);
            authNumber += String.valueOf(randomNum);
        }

        return authNumber;
    }

    // 인증번호 검증
    public String checkAuthNum(String email, String authNum) throws BaseException {

        // 이메일 존재 여부 확인
        if (!isEmptyEmail(email)) {
            throw new BaseException(ALREADY_EXIST_EMAIL);
        }

        // 인증번호 검증
        String authNumInRedis = redisService.getEmailAuthNumInRedis(email);
        if (authNumInRedis == null) {
            throw new BaseException(NOT_EXIST_AUTH_NUM_IN_REDIS);
        } else if (!authNum.equals(authNumInRedis)) {
            throw new BaseException(INVALID_AUTH_NUM);
        } else {
            return "인증이 완료되었습니다.";
        }
    }

    public void reissueAccessToken(String userId, String refreshToken, HttpServletResponse response) throws BaseException {

        if (jwtTokenService.validateToken(refreshToken)) {

            User userEntity = userRepository.findByuserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // access token 재발급
            String newAccessToken = jwtTokenService.createAccessToken(userId, userEntity.getRole());

            if (jwtTokenService.isRefreshReissue(refreshToken)) {
                // refresh token 재발급
                refreshToken = jwtTokenService.createRefreshToken(userId);
            }

            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + newAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + refreshToken);

        } else {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

    }

    // 로그아웃
    public void logout(String userId) throws BaseException {
        User userEntity = userRepository.findByuserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        // 리프래시 토큰 삭제
        redisService.deleteRefreshTokenInRedis(userId);

        // 디바이스 토큰 삭제 로직 추가!!!!!!!!!!!!!!!!!!!!!!!

    }


    public void withdrawal(String userId, String password) throws BaseException{
        Long longUserId = Long.valueOf(userId);

        // 비밀번호 검증
        User userEntity = userRepository.findByuserId(longUserId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        if (!passwordEncoder.matches(password, userEntity.getPassword())){
            throw new BaseException(INVALID_PASSWORD);
        }

        // 회원 삭제
        try {
            planetRepository.deleteByUserIdInQuery(longUserId);
            userRepository.deleteByUserIdInQuery(longUserId);
        }
        catch (Exception e){
            throw new BaseException(FAIL_WITHDRAWAL);
        }

    }
}
