package com.planz.planit.src.service;

import com.google.cloud.BaseServiceException;
import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.closet.Closet;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.goal.Goal;
import com.planz.planit.src.domain.goal.GoalMember;
import com.planz.planit.src.domain.goal.GoalMemberRole;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.inventory.Position;
import com.planz.planit.src.domain.item.BasicItem;
import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.mail.MailDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetColor;
import com.planz.planit.src.domain.user.*;
import com.planz.planit.src.domain.user.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
    private final JwtTokenService jwtTokenService;
    private final MailService mailService;
    private final RedisService redisService;
    private final DeviceTokenService deviceTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PlanetService planetService;
    private final InventoryService inventoryService;
    private final ClosetService closetService;
    private final ItemService itemService;
    private final Random random;
    private final GoalService goalService;


    @Autowired
    public UserService(UserRepository userRepository, JwtTokenService jwtTokenService, MailService mailService, RedisService redisService, @Lazy DeviceTokenService deviceTokenService, PasswordEncoder passwordEncoder, @Lazy PlanetService planetService, @Lazy InventoryService inventoryService, @Lazy ClosetService closetService, @Lazy ItemService itemService, Random random, @Lazy GoalService goalService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.mailService = mailService;
        this.redisService = redisService;
        this.deviceTokenService = deviceTokenService;
        this.passwordEncoder = passwordEncoder;
        this.planetService = planetService;
        this.inventoryService = inventoryService;
        this.closetService = closetService;
        this.itemService = itemService;
        this.random = random;
        this.goalService = goalService;
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
     * 6-1. Inventory 테이블에 기본 행성 아이템(집, 포탈) insert
     * 6-2. 포탈 : (-3.82, 3.8), 집 : (3.73, 3.29) 배치
     * 7. Closet 테이블에 기본 캐릭터 아이템(기본 옷) insert
     * 8. access token, refresh token 생성해서 헤더에 담기
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public LoginResDTO join(JoinReqDTO reqDTO, HttpServletResponse response) throws BaseException {

        try {

            // 1. 닉네임 존재 여부 확인
            if (!isEmptyNickname(reqDTO.getNickname())) {
                throw new BaseException(ALREADY_EXIST_NICKNAME);
            }

            // 2. 이메일 존재 여부 확인
            if (!isEmptyEmail(reqDTO.getEmail())) {
                throw new BaseException(ALREADY_EXIST_EMAIL);
            }

            // 3. User 테이블 insert
            User userEntity = User.builder()
                    .email(reqDTO.getEmail())
                    .password(passwordEncoder.encode(reqDTO.getPassword()))
                    .nickname(reqDTO.getNickname())
                    .characterItem(BasicItem.SPACESUIT_01.getItemId())
                    .profileColor(UserProfileColor.LightRed)
                    .point(0)
                    .missionStatus(1)
                    .userStatus(UserStatus.VALID)
                    .tmpPoint(0)
                    .prevPercent(0)
                    .role(UserRole.ROLE_USER)
                    .deviceToken(reqDTO.getDeviceToken())
                    .build();
            saveUser(userEntity);


            // 4. DeviceToken 테이블 insert
            deviceTokenService.createDeviceToken(userEntity.getUserId(), new DeviceTokenReqDTO(reqDTO.getDeviceToken()));


            // 5. Planet 테이블 insert
            Planet planetEntity = Planet.builder()
                    .user(userEntity)
                    .level(1)
                    .exp(0)
                    .color(PlanetColor.valueOf(reqDTO.getPlanetColor()))
                    .tmpExp(0)
                    .build();
            planetService.savePlanet(planetEntity);


            /// 6-1. Inventory 테이블에 기본 행성 아이템(집, 포탈) insert
            // 6-2. 포탈 : (-3.82, 3.8), 집 : (3.73, 3.29) 배치
            Item basicHouse = itemService.findItemByItemId(BasicItem.HOUSE_01.getItemId());
            Inventory basicHouseInventory = Inventory.builder()
                    .user(userEntity)
                    .planetItem(basicHouse)
                    .count(1)
                    .itemPlacement(new ArrayList<Position>())
                    .build();
            basicHouseInventory.getItemPlacement().add(
                    Position.builder()
                            .posX(3.73f)
                            .posY(3.29f)
                            .build());
            inventoryService.saveInventory(basicHouseInventory);

            Item basicPortal = itemService.findItemByItemId(BasicItem.PORTAL_00.getItemId());
            Inventory basicPortalInventory = Inventory.builder()
                    .user(userEntity)
                    .planetItem(basicPortal)
                    .count(1)
                    .itemPlacement(new ArrayList<Position>())
                    .build();

            basicPortalInventory.getItemPlacement().add(
                    Position.builder()
                            .posX(-3.82f)
                            .posY(3.8f)
                            .build());
            inventoryService.saveInventory(basicPortalInventory);


            // 7. Closet 테이블에 기본 캐릭터 아이템(기본 옷) insert
            Item basicSuit = itemService.findItemByItemId(BasicItem.SPACESUIT_01.getItemId());
            Closet basicSuitCloset = Closet.builder()
                    .user(userEntity)
                    .item(basicSuit)
                    .build();
            closetService.saveCloset(basicSuitCloset);


            // 8. access token, refresh token 생성해서 헤더에 담기
            DeviceToken findDeviceToken = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), reqDTO.getDeviceToken());

            String jwtRefreshToken = jwtTokenService.createRefreshToken(findDeviceToken.getDeviceTokenId().toString());
            String jwtAccessToken = jwtTokenService.createAccessToken(userEntity.getUserId().toString(), userEntity.getRole());

            response.addHeader(ACCESS_TOKEN_HEADER_NAME, "Bearer " + jwtAccessToken);
            response.addHeader(REFRESH_TOKEN_HEADER_NAME, "Bearer " + jwtRefreshToken);

            return LoginResDTO.builder()
                    .userId(userEntity.getUserId())
                    .planetId(planetEntity.getPlanetId())
                    .planetColor(planetEntity.getColor().name())
                    .planetLevel(planetEntity.getLevel())
                    .email(userEntity.getEmail())
                    .nickname(userEntity.getNickname())
                    .characterItem(userEntity.getCharacterItem())
                    .profileColor(userEntity.getProfileColor().name())
                    .point(userEntity.getPoint())
                    .missionStatus(userEntity.getMissionStatus())
                    .deviceToken(userEntity.getDeviceToken())
                    .exp(planetEntity.getExp())
                    .build();
        } catch (BaseException e) {
            throw e;
        }

    }


    /**
     * user 저장 혹은 업데이트
     */
    public void saveUser(User userEntity) throws BaseException {
        try {
            userRepository.save(userEntity);
        } catch (Exception e) {
            log.error("saveUser() : userRepository.save(userEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            // 1. 이메일 존재 여부 확인
            if (!isEmptyEmail(email)) {
                throw new BaseException(ALREADY_EXIST_EMAIL);
            }

            // 2. 6자리 인증번호 이메일 발송
            String myRandomNum = randomNumber(email);
            MailDTO mailDTO = MailDTO.builder()
                    .address(email)
                    .title("[PLAN-IT] 인증번호 안내")
                    .content("입력하실 6자리 인증번호는 " + myRandomNum + " 입니다.")
                    .build();
            mailService.mailSend(mailDTO);

            // 3. 인증번호 Redis에 저장
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
            // 1. 이메일 존재 여부 확인
            if (!isEmptyEmail(email)) {
                throw new BaseException(ALREADY_EXIST_EMAIL);
            }

            // 2. 인증번호 검증
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
            // 1. refresh token이 만료되었는지 확인
            if (!jwtTokenService.validateToken(refreshToken)) {
                throw new BaseException(INVALID_REFRESH_TOKEN);
            }

            // userId로 유저 객체 조회
            User userEntity = findUser(Long.valueOf(userId));

            // userId와 deviceToken 값으로 디바이스 토큰 객체 조회
            DeviceToken deviceTokenEntity = deviceTokenService.findDeviceTokenByUserIdAndDeviceToken(userEntity.getUserId(), deviceToken);
            String deviceTokenId = deviceTokenEntity.getDeviceTokenId().toString();

            // 2. header의 refresh token과 redis의 refresh token 비교
            if (!redisService.compareRefreshTokenInRedis(deviceTokenId, refreshToken)) {
                throw new BaseException(TWO_REFRESH_TOKEN_NOT_EQUAL);
            }

            // 3. access token 재발급
            String newAccessToken = jwtTokenService.createAccessToken(userId, userEntity.getRole());

            if (jwtTokenService.isRefreshReissue(refreshToken)) {
                // 4. refresh token 재발급
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
            User userEntity = findUser(longUserId);

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
            User userEntity = findUser(longUserId);
            if (!passwordEncoder.matches(password, userEntity.getPassword())) {
                throw new BaseException(INVALID_PASSWORD);
            }

            // refresh token 삭제
            List<Long> findDeviceTokenIdList = deviceTokenService.findUserAllDeviceToken(userEntity.getUserId());
            for (Long deviceTokenId : findDeviceTokenIdList) {
                redisService.deleteRefreshTokenInRedis(deviceTokenId.toString());
            }

            // Goal 삭제
            goalService.deleteGoals(longUserId, GoalMemberRole.MANAGER);

            // User 삭제
            deleteUser(longUserId);

        } catch (BaseException e) {
            throw e;
        }

    }

    /**
     * User 삭제
     */
    public void deleteUser(Long longUserId) throws BaseException {
        try {
            userRepository.deleteByUserIdInQuery(longUserId);
        }
        catch (Exception e){
            log.error("deleteUser() : userRepository.deleteByUserIdInQuery(longUserId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
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



    /**
     * 랜덤으로 10자리 임시 비밀번호 생성
     */
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
            User userEntity = findUser(Long.valueOf(userId));

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
     * 1. 닉네임 중복 확인
     * 2. DB에 새로운 닉네임 저장
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void modifyNickname(String userId, String nickname) throws BaseException {

        try {
            User userEntity = findUser(Long.valueOf(userId));

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


     /* 프로필 색상 변경
     * 1. 유저 조회
     * 2. User의 profileColor 변경
     * 3. DB에 저장
     */
     @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void modifyProfile(Long userId, String profileColor) throws BaseException{
        try{
            // 1. 유저 조회
            User userEntity = findUser(userId);

            // 2. User의 profileColor 변경
            userEntity.setProfileColor(UserProfileColor.of(profileColor));

            // 3. DB에 저장
            saveUser(userEntity);
        }
        catch (BaseException e){
            throw e;
        }

    }

    /**
     * 친구 검색 API
     * @param keyword
     * @return
     * @throws BaseException
     */
    //닉네임 혹은 이메일로 유저 검색
    public SearchUserResDTO searchUsers(String keyword) throws BaseException {
        //유저 검색
        User userEntity = userRepository.findByNicknameOrEmail(keyword, keyword).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        //행성 검색 (행성 레벨)
        Planet userPlanet = planetService.findPlanetByUserId(userEntity.getUserId());

        try {
            //dto 만들기
            return new SearchUserResDTO(userEntity.getUserId(), userEntity.getNickname(), userEntity.getProfileColor().toString(), userPlanet.getLevel());
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_SEARCH_USER);
        }
    }

    /**
     * 그룹 목표 생성 시 친구 닉네임 검색
     * @param nickname
     * @return
     * @throws BaseException
     */
    public GoalSearchUserResDTO goalSearchUsers(String nickname) throws BaseException {
        //유저 검색
        User findUser = userRepository.findByNickname(nickname).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        return new GoalSearchUserResDTO(findUser.getUserId(), findUser.getNickname(),findUser.getProfileColor().toString());
    }



    /**
     * 현재 사용중인 캐릭터 아이템(옷) 변경
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void changeCharacterItem(User user, Long itemId) throws BaseException {
        try{
            user.setCharacterItem(itemId);
            saveUser(user);
        }
        catch (BaseException e){
            throw e;
        }
    }


    /**
     * 운영자 미션 받기 설정 API
     * 운영자 미션 받기 여부를 변경한다.
     * 1. 유저 찾기
     * 2. 해당 유저의 missionStatus 값 변경
     * 3. 다시 저장
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void convertMissionStatus(Long userId, int status) throws BaseException{

        try{
            // 1. 유저 찾기
            User user = findUser(userId);

            // 2. 해당 유저의 missionStatus 값 변경
            user.setMissionStatus(status);

            // 3. 다시 저장
            saveUser(user);

            //4. 미션 상태 변경
            goalService.changeToArchiveMission(userId);
        }
        catch (BaseException e){
            throw e;
        }

    }

    /**
     * 모든 유저 검색
     */
    public List<User> findAllUser() throws BaseException {
        try{
            return userRepository.findAll();
        }
        catch (Exception e){
            log.error("findAllUser() : userRepository.findAll() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetUserResultResDTO getUserResult(Long userId) throws BaseException {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(NOT_EXIST_USER));
        Planet planet = planetService.findPlanetByUserId(userId);
        return new GetUserResultResDTO(planet.getExp(), user.getPoint());
    }
}
