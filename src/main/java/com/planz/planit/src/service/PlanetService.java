package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.inventory.Position;
import com.planz.planit.src.domain.inventory.dto.ItemPositionDTO;
import com.planz.planit.src.domain.inventory.dto.PositionDTO;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetRepository;
import com.planz.planit.src.domain.planet.dto.GetPlanetMainInfoResDTO;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.config.BaseResponseStatus.OVER_ITEM_COUNT;

@Log4j2
@Service
public class PlanetService {
    private final PlanetRepository planetRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final InventoryService inventoryService;

    @Autowired
    public PlanetService(PlanetRepository planetRepository, UserService userService, FriendService friendService, InventoryService inventoryService) {
        this.planetRepository = planetRepository;
        this.userService = userService;
        this.friendService = friendService;
        this.inventoryService = inventoryService;
    }


    // Planet 저장
    public void savePlanet(Planet planetEntity) throws BaseException {
        try {
            planetRepository.save(planetEntity);
        } catch (Exception e) {
            log.error("savePlanet() : planetRepository.save(planetEntity) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // Planet 삭제
    public void deletePlanet(Long longUserId) throws BaseException {
        try {
            planetRepository.deleteByUserIdInQuery(longUserId);
        } catch (Exception e) {
            log.error("deletePlanet() : planetRepository.deleteByUserIdInQuery(longUserId) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // Planet 조회
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

    // 행성 메인 화면 정보 조회회
    public GetPlanetMainInfoResDTO getPlanetMainInfo(Long targetUserId) throws BaseException {

        try {

            // 타켓 유저 조회
            User user = userService.findUser(targetUserId);

            // 타겟 유저의 행성 조회
            Planet planet = findPlanetByUserId(targetUserId);

            // 타겟 유저가 현재 사용중인 행성 아이템 목록 조회
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
                                    .itemId(inventory.getPlanetItem().getItemId())
                                    .positionList(positionList)
                                    .build());
                }
            }

            return GetPlanetMainInfoResDTO.builder()
                    .level(planet.getLevel())
                    .characterItem(user.getCharacterItem())
                    .planetItemList(planetItemList)
                    .build();

        } catch (BaseException e) {
            throw e;
        }
    }
}
