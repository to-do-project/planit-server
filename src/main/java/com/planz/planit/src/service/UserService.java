package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.mail.MailDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetColor;
import com.planz.planit.src.domain.planet.PlanetRepository;
import com.planz.planit.src.domain.user.*;
import com.planz.planit.src.domain.user.dto.JoinReqDTO;
import com.planz.planit.src.domain.user.dto.LoginResDTO;
import com.planz.planit.src.domain.user.dto.SearchUserResDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
    private final DeviceTokenService deviceTokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PlanetRepository planetRepository, JwtTokenService jwtTokenService, MailService mailService, RedisService redisService, @Lazy DeviceTokenService deviceTokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.planetRepository = planetRepository;
        this.jwtTokenService = jwtTokenService;
        this.mailService = mailService;
        this.redisService = redisService;
        this.deviceTokenService = deviceTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    //은지 추가 코드
    public User findUser(Long userId) throws BaseException {
        return userRepository.findById(userId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
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
            System.out.println("구역 1 - 유저 저장");
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

            System.out.println("구역 2 - 디바이스 토큰 저장");
            // request로 받은 device token을 DB에 저장하기!!!!! => 수정 필요!!!
            deviceTokenService.createDeviceToken(userEntity.getUserId(), new DeviceTokenReqDTO(reqDTO.getDeviceToken()));


            System.out.println("구역 3 - 플래닛 저장");
            // Planet 저장
            Planet planetEntity = Planet.builder()
                    .user(userEntity)
                    .level(0)
                    .exp(0)
                    .color(PlanetColor.valueOf(reqDTO.getPlanetColor()))
                    .build();

            planetRepository.save(planetEntity);


            System.out.println("구역 4.1 - jwt 토큰 생성1");
            // access token, refresh token 생성해서 헤더에 담기
            DeviceToken findDeviceToken = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), reqDTO.getDeviceToken());

            System.out.println("구역 4.2 - jwt 토큰 생성2");
            String jwtRefreshToken = jwtTokenService.createRefreshToken(findDeviceToken.getDeviceTokenId().toString());

            System.out.println("구역 4.3 - jwt 토큰 생성3");
            String jwtAccessToken = jwtTokenService.createAccessToken(userEntity.getUserId().toString(), userEntity.getRole());

            System.out.println("구역 5 - 헤더에 담기");
            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + jwtAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + jwtRefreshToken);

            System.out.println("구역 6 - 리턴");
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

    public void reissueAccessToken(String userId, String refreshToken, String deviceToken, HttpServletResponse response) throws BaseException {

        // refresh token이 만료되었는지 확인
        if (jwtTokenService.validateToken(refreshToken)) {

            // userId로 유저 객체 조회
            User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // userId와 deviceToken 값으로 디바이스 토큰 객체 조회
            DeviceToken deviceTokenEntity = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), deviceToken);
            String deviceTokenId = deviceTokenEntity.getDeviceTokenId().toString();

            // header의 refresh token과 redis의 refresh token 비교
            if (!redisService.compareRefreshTokenInRedis(deviceTokenId, refreshToken)){
                throw new BaseException(TWO_REFRESH_TOKEN_NOT_EQUAL);
            }

            // access token 재발급
            String newAccessToken = jwtTokenService.createAccessToken(userId, userEntity.getRole());

            if (jwtTokenService.isRefreshReissue(refreshToken)) {
                // refresh token 재발급
                refreshToken = jwtTokenService.createRefreshToken(deviceTokenId);
            }

            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + newAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + refreshToken);

        } else {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

    }

    // 로그아웃
    public void logout(String userId, DeviceTokenReqDTO reqDTO) throws BaseException {
        Long longUserId = Long.valueOf(userId);
        User userEntity = userRepository.findByUserId(longUserId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        // 리프래시 토큰 삭제
        DeviceToken findDeviceToken = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), reqDTO.getDeviceToken());
        redisService.deleteRefreshTokenInRedis(findDeviceToken.getDeviceTokenId().toString());

        // 디바이스 토큰 삭제 로직 추가!!!!!!!!!!!!!!!!!!!!!!!
        deviceTokenService.deleteDeviceToken(longUserId, reqDTO);
    }


    public void withdrawal(String userId, String password) throws BaseException {
        Long longUserId = Long.valueOf(userId);

        // 비밀번호 검증
        User userEntity = userRepository.findByUserId(longUserId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new BaseException(INVALID_PASSWORD);
        }

        // 회원 삭제
        try {
            // 리프래시 토큰 삭제 +. 수정 필요!!!
            List<Long> findDeviceTokenIdList = deviceTokenService.findUserAllDeviceToken(userEntity.getUserId());
            for (Long deviceTokenId : findDeviceTokenIdList) {
                redisService.deleteRefreshTokenInRedis(deviceTokenId.toString());
            }

            deviceTokenService.deleteAllDeviceToken(longUserId);
            planetRepository.deleteByUserIdInQuery(longUserId);
            userRepository.deleteByUserIdInQuery(longUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_WITHDRAWAL);
        }

    }

    // 임시 비밀번호 발송
    @Transactional
    public void createTemporaryPwd(String email) throws BaseException{

        User userEntity = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        String myRandomPwd = randomPwd(email);

        // 이메일로 임시 pwd 전송
        try {
            MailDTO mailDTO = MailDTO.builder()
                    .address(email)
                    .title("[PLAN-IT] 임시 비밀번호 안내")
                    .content("발급받으신 임시 비밀번호는 " + myRandomPwd + " 입니다.")
                    .build();

            mailService.mailSend(mailDTO);
        }
        catch (BaseException e){
            throw e;
        }

        // DB에 임시 pwd 저장
        try{
            userEntity.setPassword(passwordEncoder.encode(myRandomPwd));
            userRepository.save(userEntity);
        }
        catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // 랜덤으로 10자리 임시 비밀번호 생성
    public String randomPwd(String email){

        Random random = new Random(System.currentTimeMillis() + email.length());
        StringBuffer password = new StringBuffer();

        char[] charSet = new char[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };	//배열안의 문자 숫자는 원하는대로

        int i, randomIndex;
        for (i = 0; i < 10; i++) {
            randomIndex = random.nextInt(charSet.length);
            password.append(charSet[randomIndex]);
        }

        return password.toString();
    }

    // 비밀번호 변경
    public void modifyPassword(String userId, String oldPassword, String newPassword) throws BaseException {

        User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        // 기존 비밀번호가 올바른지 확인
        if(!passwordEncoder.matches(oldPassword, userEntity.getPassword())){
            throw new BaseException(INVALID_PASSWORD);
        }

        // DB에 새로운 pwd 저장
        try{
            userEntity.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(userEntity);
        }
        catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 닉네임 변경
    public void modifyNickname(String userId, String nickname) throws BaseException {

        User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        // 닉네임 중복확인
        if (!isEmptyNickname(nickname)) {
            throw new BaseException(ALREADY_EXIST_NICKNAME);
        }

        // DB에 새로운 닉네임 저장
        try{
            userEntity.setNickname(nickname);
            userRepository.save(userEntity);
        }
        catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //닉네임 혹은 이메일로 유저 검색
    public SearchUserResDTO searchUsers(String keyword) throws BaseException {
        //유저 검색
        User userEntity = userRepository.findByNicknameOrEmail(keyword,keyword).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        //행성 검색 (행성 레벨)
        Planet userPlanet = planetRepository.findByUserId(userEntity.getUserId()).orElseThrow(() -> new BaseException(NOT_EXIST_PLANET_INFO));

        try{
            //dto 만들기
            return new SearchUserResDTO(userEntity.getUserId(),userEntity.getNickname(),userEntity.getProfileColor().toString(),userPlanet.getLevel());
        }catch(Exception e){
            throw new BaseException(FAILED_TO_SEARCH_USER);
        }

    }
}
