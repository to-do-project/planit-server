package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.inventory.Position;
import com.planz.planit.src.domain.inventory.dto.ItemPositionDTO;
import com.planz.planit.src.domain.inventory.dto.PositionDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetRepository;
import com.planz.planit.src.domain.planet.dto.GetPlanetMainInfoResDTO;
import com.planz.planit.src.domain.planet.dto.GetPlanetMyInfoResDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
@Service
public class PlanetService {
    private final PlanetRepository planetRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final InventoryService inventoryService;
    private final TodoService todoService;

    @Autowired
    public PlanetService(PlanetRepository planetRepository, UserService userService, FriendService friendService, InventoryService inventoryService, TodoService todoService) {
        this.planetRepository = planetRepository;
        this.userService = userService;
        this.friendService = friendService;
        this.inventoryService = inventoryService;
        this.todoService = todoService;
    }


    /**
     * Planet 저장
     */
    public void savePlanet(Planet planetEntity) throws BaseException {
        try {
            planetRepository.save(planetEntity);
        } catch (Exception e) {
            log.error("savePlanet() : planetRepository.save(planetEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * Planet 조회
     */
    public Planet findPlanetByUserId(Long userId) throws BaseException {
        try {
            return planetRepository.findByUserId(userId).orElseThrow(() -> new BaseException(NOT_EXIST_PLANET_INFO));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("findPlanetByUserId() : planetRepository.findByUserId(userId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 행성 메인 화면 조회 API
     * 타겟 유저의 행성 관련 정보를 모두 반환한다.
     * (행성 레벨, 현재 사용중인 캐릭터 아이템 아이디, 현재 사용중인 행성 아이템 아이디 + 위치) 반환
     * 1. 타켓 유저 조회
     * 2. 타겟 유저의 행성 조회
     * 3. 타겟 유저가 현재 사용중인 행성 아이템 목록 조회
     * 4. 결과 반환
     */
    public GetPlanetMainInfoResDTO getPlanetMainInfo(Long targetUserId) throws BaseException {

        try {
            // 1. 타켓 유저 조회
            User user = userService.findUser(targetUserId);

            boolean isRunAway = false;
            if (user.getUserStatus() == UserStatus.RUN_AWAY){
                isRunAway = true;
            }

            // 2. 타겟 유저의 행성 조회
            Planet planet = findPlanetByUserId(targetUserId);

            // 3. 타겟 유저가 현재 사용중인 행성 아이템 목록 조회
            List<ItemPositionDTO> planetItemList = new ArrayList<>();

            List<Inventory> inventoryList = inventoryService.findInventoryListByUser(user);
            for (Inventory inventory : inventoryList) {

                if (inventory.getItemPlacement().size() > 0) {
                    List<PositionDTO> positionList = new ArrayList<>();
                    for (Position position : inventory.getItemPlacement()) {
                        positionList.add(
                                PositionDTO.builder()
                                        .posX(position.getPosX())
                                        .posY(position.getPosY())
                                        .build());
                    }

                    planetItemList.add(
                            ItemPositionDTO.builder()
                                    .itemCode(inventory.getPlanetItem().getCode())
                                    .positionList(positionList)
                                    .build());
                }
            }

            // 4. 결과 반환
            return GetPlanetMainInfoResDTO.builder()
                    .userId(targetUserId)
                    .planetColor(planet.getColor().name())
                    .level(planet.getLevel())
                    .prePercent(user.getPrevPercent())
                    .isRunAway(isRunAway)
                    .characterItem(user.getCharacterItem())
                    .planetItemList(planetItemList)
                    .build();

        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * 나의 행성 정보 조회 API
     * (행성 나이, 행성 레벨, 보유 포인트, 평균 목표 완성률, 좋아요 받은 수, 좋아요 누른 수) 반환
     * 1. 유저 조회
     * 2. 행성 조회
     * 3. 행성 나이 계산
     * 4. 평균 목표 완성률 계산
     * 5. 좋아요 받은 수 계산
     * 6. 좋아요 누른 수 계산
     */
    public GetPlanetMyInfoResDTO getPlanetMyInfo(Long userId) throws BaseException{
        try {
            // 1. 유저 조회
            User user = userService.findUser(userId);

            // 2. 행성 조회
            Planet planet = user.getPlanet();

            // 3. 행성 나이 계산
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime createAt = user.getCreateAt();
            long age = ChronoUnit.DAYS.between(createAt, today) + 1;

            // 4. 목표량 계산 -> PASS!

            // 5. 좋아요 받은 수 계산
            int totalTodoLike = todoService.getTotalTodoLike(userId);
            // 6. 좋아요 누른 수 계산
            int pushTotalTodoLike = todoService.getPushTotalTodoLike(userId);

            return GetPlanetMyInfoResDTO.builder()
                    .age(age)
                    .level(planet.getLevel())
                    .point(user.getPoint())
                    .avgGoalCompleteRate(user.getPrevPercent())
                    .getFavoriteCount(totalTodoLike)
                    .putFavoriteCount(pushTotalTodoLike)
                    .build();
        }
        catch (BaseException e){
            throw e;
        }
    }
}
