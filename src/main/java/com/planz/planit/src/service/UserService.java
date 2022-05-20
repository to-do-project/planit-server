package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.item.CharacterItem;
import com.planz.planit.src.domain.mail.MailDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetColor;
import com.planz.planit.src.domain.planet.PlanetRepository;
import com.planz.planit.src.domain.user.*;
import com.planz.planit.src.domain.user.dto.JoinReqDTO;
import com.planz.planit.src.domain.user.dto.LoginResDTO;
import com.planz.planit.src.domain.user.dto.SearchUserResDTO;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
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

    char[] charSet = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };    //배열안의 문자 숫자는 원하는대로

    private final UserRepository userRepository;
    private final PlanetRepository planetRepository;
    private final JwtTokenService jwtTokenService;
    private final MailService mailService;
    private final RedisService redisService;
    private final DeviceTokenService deviceTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PlanetService planetService;
    private final Random random;

    @Autowired
    public UserService(UserRepository userRepository, PlanetRepository planetRepository, JwtTokenService jwtTokenService, MailService mailService, RedisService redisService, @Lazy DeviceTokenService deviceTokenService, PasswordEncoder passwordEncoder, PlanetService planetService, Random random) {
        this.userRepository = userRepository;
        this.planetRepository = planetRepository;
        this.jwtTokenService = jwtTokenService;
        this.mailService = mailService;
        this.redisService = redisService;
        this.deviceTokenService = deviceTokenService;
        this.passwordEncoder = passwordEncoder;
        this.planetService = planetService;
        this.random = random;
    }

    //은지 추가 코드
    public User findUser(Long userId) throws BaseException {
        return userRepository.findById(userId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
    }


    /**
     * 회원가입
     * 1. 닉네임 존재 여부 확인
     * 2. 이메일 존재 여부 확인
     * 3. User 테이블 insert
     * 4. DeviceToken 테이블 insert
     * 5. Planet 테이블 insert
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public LoginResDTO join(JoinReqDTO reqDTO, HttpServletResponse response) throws BaseException {

        try {

            // 닉네임 존재 여부 확인
            if (!isEmptyNickname(reqDTO.getNickname())) {
                throw new BaseException(ALREADY_EXIST_NICKNAME);
            }

            // 이메일 존재 여부 확인
            if (!isEmptyEmail(reqDTO.getEmail())) {
                throw new BaseException(ALREADY_EXIST_EMAIL);
            }

            // User 테이블 insert
            User userEntity = User.builder()
                    .email(reqDTO.getEmail())
                    .password(passwordEncoder.encode(reqDTO.getPassword()))
                    .nickname(reqDTO.getNickname())
                    .characterItem(CharacterItem.SPACESUIT_01.getItemId())
                    .profileColor(UserProfileColor.WHITE)
                    .point(0)
                    .missionStatus(1)
                    .userStatus(UserStatus.VALID)
                    .role(UserRole.ROLE_USER)
                    .deviceToken(reqDTO.getDeviceToken())
                    .build();
            saveUser(userEntity);


            // DeviceToken 테이블 insert
            deviceTokenService.createDeviceToken(userEntity.getUserId(), new DeviceTokenReqDTO(reqDTO.getDeviceToken()));


            // Planet 테이블 insert
            Planet planetEntity = Planet.builder()
                    .user(userEntity)
                    .level(0)
                    .exp(0)
                    .color(PlanetColor.valueOf(reqDTO.getPlanetColor()))
                    .build();
            planetService.savePlanet(planetEntity);


            // access token, refresh token 생성해서 헤더에 담기
            DeviceToken findDeviceToken = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), reqDTO.getDeviceToken());

            String jwtRefreshToken = jwtTokenService.createRefreshToken(findDeviceToken.getDeviceTokenId().toString());
            String jwtAccessToken = jwtTokenService.createAccessToken(userEntity.getUserId().toString(), userEntity.getRole());

            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + jwtAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + jwtRefreshToken);

            return LoginResDTO.builder()
                    .userId(userEntity.getUserId())
                    .planetId(planetEntity.getPlanetId())
                    .email(userEntity.getEmail())
                    .nickname(userEntity.getNickname())
                    .characterItem(userEntity.getCharacterItem())
                    .profileColor(userEntity.getProfileColor().name())
                    .point(userEntity.getPoint())
                    .missionStatus(userEntity.getMissionStatus())
                    .deviceToken(userEntity.getDeviceToken())
                    .build();
        } catch (BaseException e) {
            throw e;
        }

    }


    public void saveUser(User userEntity) throws BaseException {
        try {
            userRepository.save(userEntity);
        } catch (Exception e) {
            log.error("saveUser() : userRepository.save(userEntity) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * DB에 존재하는 이메일인지 확인
     */
    public boolean isEmptyEmail(String email) throws BaseException {
        try {
            return userRepository.findByEmail(email).isEmpty();
        } catch (Exception e) {
            log.error("isEmptyEmail() : userRepository.findByEmail() 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * DB에 존재하는 닉네임인지 확인
     */
    public boolean isEmptyNickname(String nickname) throws BaseException {
        try {
            return userRepository.findByNickname(nickname).isEmpty();
        } catch (Exception e) {
            log.error("isEmptyNickname() : userRepository.findByNickname() 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 이메일 인증번호 발송
     * 1. 이메일 존재 여부 확인
     * 2. 이메일로 인증번호 발송
     * 3. 생성한 인증번호 Redis에 저장
     */
    public void createAuthNum(String email) throws BaseException {

        try {
            // 이메일 존재 여부 확인
            if (!isEmptyEmail(email)) {
                throw new BaseException(ALREADY_EXIST_EMAIL);
            }

            // 6자리 인증번호 이메일 발송
            String myRandomNum = randomNumber(email);
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

    /**
     * 6자리 랜덤 인증번호 생성
     */
    public String randomNumber(String email) {
        String authNumber = "";
        random.setSeed(System.currentTimeMillis() + email.length());

        int i, randomNum;
        for (i = 0; i < 6; i++) {
            randomNum = random.nextInt(10);
            authNumber += String.valueOf(randomNum);
        }

        return authNumber;
    }

    /**
     * 이메일 인증번호 검증
     * 1. 이메일 존재 여부 확인
     * 2. 이메일 인증번호 검증
     */
    public void checkAuthNum(String email, String authNum) throws BaseException {
        try {
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
            }
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * access token & refresh token을 재발급해준다.
     * 1. refresh token이 만료되었는지 확인
     * 2. header의 refresh token과 redis의 refresh token 비교
     * 3. access token 재발급해서 헤더에 담기
     * 4. refresh token 재발급해서 헤더에 담기 => refresh token의 유효기간이 2일 이하로 남았을 경우
     */
    public void reissueAccessToken(String userId, String refreshToken, String deviceToken, HttpServletResponse response) throws BaseException {

        try {
            // refresh token이 만료되었는지 확인
            if (!jwtTokenService.validateToken(refreshToken)) {
                throw new BaseException(INVALID_REFRESH_TOKEN);
            }

            // userId로 유저 객체 조회
            User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // userId와 deviceToken 값으로 디바이스 토큰 객체 조회
            DeviceToken deviceTokenEntity = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), deviceToken);
            String deviceTokenId = deviceTokenEntity.getDeviceTokenId().toString();

            // header의 refresh token과 redis의 refresh token 비교
            if (!redisService.compareRefreshTokenInRedis(deviceTokenId, refreshToken)) {
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


        } catch (BaseException e) {
            throw e;
        }

    }

    /**
     * 로그아웃한다.
     * 1. redis에 저장된 jwt refresh token 삭제
     * 2. 디바이스 토큰 삭제
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void logout(String userId, DeviceTokenReqDTO reqDTO) throws BaseException {
        try {
            Long longUserId = Long.valueOf(userId);
            User userEntity = userRepository.findByUserId(longUserId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // 리프래시 토큰 삭제
            DeviceToken findDeviceToken = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), reqDTO.getDeviceToken());
            redisService.deleteRefreshTokenInRedis(findDeviceToken.getDeviceTokenId().toString());

            // 디바이스 토큰 삭제 로직 추가!!!!!!!!!!!!!!!!!!!!!!!
            deviceTokenService.deleteDeviceToken(longUserId, reqDTO);
        }
        catch (BaseException e){
            throw e;
        }
    }

    /**
     * 회원탈퇴한다.
     * 1. 비밀번호 검증
     * 2. redis에 저장된 refresh token삭제
     * 3. DeviceToken 삭제
     * 4. Planet 삭제
     * 5. User 삭제
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void withdrawal(String userId, String password) throws BaseException {

        try {
            Long longUserId = Long.valueOf(userId);

            // 비밀번호 검증
            User userEntity = userRepository.findByUserId(longUserId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
            if (!passwordEncoder.matches(password, userEntity.getPassword())) {
                throw new BaseException(INVALID_PASSWORD);
            }

            // refresh token 삭제
            List<Long> findDeviceTokenIdList = deviceTokenService.findUserAllDeviceToken(userEntity.getUserId());
            for (Long deviceTokenId : findDeviceTokenIdList) {
                redisService.deleteRefreshTokenInRedis(deviceTokenId.toString());
            }

            // DeviceToken 삭제
            deviceTokenService.deleteAllDeviceToken(longUserId);

            // Planet 삭제
            planetService.deletePlanet(longUserId);

            // User 삭제
            deleteUser(longUserId);

        } catch (BaseException e) {
            throw e;
        }

    }

    public void deleteUser(Long longUserId) throws BaseException {
        try {
            userRepository.deleteByUserIdInQuery(longUserId);
        }
        catch (Exception e){
            log.error("deleteUser() : userRepository.deleteByUserIdInQuery(longUserId) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(FAIL_WITHDRAWAL);
        }
    }


    /**
     * 임시 비밀번호를 발송한다.
     * 1. 이메일로 임시 pwd 전송
     * 2. DB에 임시 pwd 저장
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void createTemporaryPwd(String email) throws BaseException {

        User userEntity = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

        String myRandomPwd = randomPwd(email);

        try {
            // 이메일로 임시 pwd 전송
            MailDTO mailDTO = MailDTO.builder()
                    .address(email)
                    .title("[PLAN-IT] 임시 비밀번호 안내")
                    .content("발급받으신 임시 비밀번호는 " + myRandomPwd + " 입니다.")
                    .build();

            mailService.mailSend(mailDTO);

            // DB에 임시 pwd 저장
            userEntity.setPassword(passwordEncoder.encode(myRandomPwd));
            saveUser(userEntity);

        } catch (BaseException e) {
            throw e;
        }
    }


    // 랜덤으로 10자리 임시 비밀번호 생성
    public String randomPwd(String email) {

        random.setSeed(System.currentTimeMillis() + email.length());
        StringBuffer password = new StringBuffer();

        int i, randomIndex;

        // 임시 비밀번호에 숫자 하나 추가
        randomIndex = random.nextInt(10);
        password.append(charSet[randomIndex]);

        // 임시 비밀번호에 영문 하나 추가가
        randomIndex = random.nextInt(charSet.length-10);
        password.append(charSet[randomIndex+10]);

        for (i = 0; i < 8; i++) {
            randomIndex = random.nextInt(charSet.length);
            password.append(charSet[randomIndex]);
        }

        return password.toString();
    }


    /**
     * 비밀번호 변경
     * 1. 기존 비밀번호가 올바른지 확인
     * 2. DB에 새로운 pwd 저장
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void modifyPassword(String userId, String oldPassword, String newPassword) throws BaseException {

        try {
            User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // 기존 비밀번호가 올바른지 확인
            if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
                throw new BaseException(INVALID_PASSWORD);
            }

            // DB에 새로운 pwd 저장
            userEntity.setPassword(passwordEncoder.encode(newPassword));
            saveUser(userEntity);

        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * 닉네임 변경
     * 1. 기존 비밀번호가 올바른지 확인
     * 2. DB에 새로운 pwd 저장
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void modifyNickname(String userId, String nickname) throws BaseException {

        try {
            User userEntity = userRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new BaseException(NOT_EXIST_USER));

            // 닉네임 중복확인
            if (!isEmptyNickname(nickname)) {
                throw new BaseException(ALREADY_EXIST_NICKNAME);
            }

            // DB에 새로운 닉네임 저장
            userEntity.setNickname(nickname);
            saveUser(userEntity);

        } catch (BaseException e) {
            throw e;
        }
    }

    //닉네임 혹은 이메일로 유저 검색
    public SearchUserResDTO searchUsers(String keyword) throws BaseException {
        //유저 검색
        User userEntity = userRepository.findByNicknameOrEmail(keyword, keyword).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        //행성 검색 (행성 레벨)
        Planet userPlanet = planetRepository.findByUserId(userEntity.getUserId()).orElseThrow(() -> new BaseException(NOT_EXIST_PLANET_INFO));

        try {
            //dto 만들기
            return new SearchUserResDTO(userEntity.getUserId(), userEntity.getNickname(), userEntity.getProfileColor().toString(), userPlanet.getLevel());
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_SEARCH_USER);
        }

    }

    // 현재 사용중인 캐릭터 아이템(옷) 변경
    public void changeCharacterItem(User user, Long itemId) throws BaseException {
        try{
            user.setCharacterItem(itemId);
            saveUser(user);
        }
        catch (BaseException e){
            throw e;
        }
    }
}
