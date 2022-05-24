package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.inventory.InventoryRepository;
import com.planz.planit.src.domain.inventory.Position;
import com.planz.planit.src.domain.inventory.dto.GetInventoryResDTO;
import com.planz.planit.src.domain.inventory.dto.ItemPositionDTO;
import com.planz.planit.src.domain.inventory.dto.PositionDTO;
import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.item.ItemCategory;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;

@Service
@Log4j2
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ItemService itemService, UserService userService) {
        this.inventoryRepository = inventoryRepository;
        this.itemService = itemService;
        this.userService = userService;
    }

    /**
     * 카테고리 별로 인벤토리에서 보유 중인 행성 아이템 목록을 반환한다.
     * => List(inventoryId, itemId, totalCount, placedCount, remainingCount) 반환
     *
     * @param userId, category
     * @return List<GetInventoryResDTO>
     * @throws BaseException
     */
    public List<GetInventoryResDTO> getInventoryByCategory(Long userId, String category) throws BaseException {
        try {
            List<GetInventoryResDTO> result = new ArrayList<GetInventoryResDTO>();

            List<Inventory> inventoryList = findInventoryItemsByCategory(userId, ItemCategory.valueOf(category));

            for (Inventory inventory : inventoryList) {

                int totalCount = inventory.getCount();
                int placedCount = inventory.getItemPlacement().size();

                result.add(GetInventoryResDTO.builder()
                        .itemId(inventory.getPlanetItem().getItemId())
                        .totalCount(totalCount)
                        .placedCount(placedCount)
                        .remainingCount(totalCount - placedCount)
                        .build());
            }

            return result;
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * userId와 category를 이용해서, 인벤토리 리스트 조회하기
     * => 특정 사용자의 모든 인벤토리 정보를 카테고리 별로 조회하기
     */
    public List<Inventory> findInventoryItemsByCategory(Long userId, ItemCategory category) throws BaseException {
        try {
            return inventoryRepository.findInventoryItemsByCategory(userId, category);
        } catch (Exception e) {
            log.error("findInventoryItemsByCategory() : inventoryRepository.findInventoryItemsByCategory(userId, category) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*// 행성 아이템 구매
    @Transactional
    public void buyPlanetItem(String userId, Long itemId, Integer count) throws BaseException {

        try {

            Long longUserId = Long.valueOf(userId);

            // itemId에 대한 논리적 validation => 실제로 존재하는 행성 아이템 아이디인지
            Item planetItem = itemService.findPlanetItemById(itemId);

            // point에 대한 논리적 validation => 현재 보유중인 포인트가 부족하지 않은지
            int totalPrice = planetItem.getPrice() * count;
            User user = userService.findUser(longUserId);
            if (user.getPoint() < totalPrice) {
                throw new BaseException(LACK_OF_POINT);
            }


            Inventory inventory = getInventory(planetItem, user);

            // count에 대한 논리적 validation => 아이템 개수는 maxCnt개 이상 보유할 수 없다.
            if (!checkItemMaxCnt(inventory, count)) {
                throw new BaseException(MAX_ITEM_COUNT);
            }


            // 행성 아이템 구매
            // 1. 인벤토리 튜플 생성 혹은 count값 수정
            inventory.setCount(inventory.getCount() + count);
            saveInventory(inventory);

            // 2. User의 point 갱신
            user.setPoint(user.getPoint() - totalPrice);
            userService.saveUser(user);


        } catch (BaseException e) {
            throw e;
        }

    }
*/
    public Inventory findInventoryByUserAndItem(User user, Item planetItem) throws BaseException {
        try {
            Inventory inventory = inventoryRepository.findByUserAndPlanetItem(user, planetItem).orElseGet(
                    () -> Inventory.builder()
                            .user(user)
                            .planetItem(planetItem)
                            .count(0)
                            .build());
            return inventory;
        } catch (Exception e) {
            log.error("findInventoryByUserAndItem() : inventoryRepository.findByUserAndPlanetItem(user, planetItem) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // inventory 저장
    public void saveInventory(Inventory inventoryEntity) throws BaseException {
        try {
            inventoryRepository.save(inventoryEntity);
        } catch (Exception e) {
            log.error("saveInventory() : inventoryRepository.save(inventoryEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

   /* // 행성 아이템을 더 구매할 수 있는지 확인
    public boolean checkItemMaxCnt(Inventory inventory, int numberOfPurchases) {

        if (inventory.getCount() + numberOfPurchases <= inventory.getPlanetItem().getMaxCnt()) {
            return true;
        } else {
            return false;
        }
    }*/


    /**
     * 보유한 행성 아이템 (기본 건축물, 식물, 돌, 길, 기타)을 배치한다. => 행성에 적용
     *
     * @param userId, InventoryPositionDTOList
     *                => userid, inventoryPositionList(inventoryId, positionList(posX, posY))
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void placePlanetItems(Long userId, List<ItemPositionDTO> itemPositionDTOList) throws BaseException {

        try {

            // 1. 유저 정보 조회 => 존재하지 않는 아이템이면 BaseException throw
            User user = userService.findUser(userId);

            // 2. 기존 아이템 배치 삭제
            List<Inventory> inventoryList = findInventoryListByUser(user);
            HashMap<Long, Inventory> inventoryHashMap = new HashMap<>();
            for (Inventory inventory : inventoryList) {
                inventory.getItemPlacement().clear();
                inventoryHashMap.put(inventory.getPlanetItem().getItemId(), inventory);
            }

            // 3. 새로운 아이템 배치 저장
            for (ItemPositionDTO itemPositionDTO : itemPositionDTOList) {

                // request로 받은 inventoryId로, 유저가 가지고 있는 Inventory 데이터 조회
                Inventory findInventory = inventoryHashMap.remove(itemPositionDTO.getItemId());
                if (findInventory == null) {
                    // 해당 유저가 보유한 인벤토리 아이템이 맞는지 확인
                    // request에 itemId를 중복으로 입력했는지 확인
                    throw new BaseException(NOT_OWN_OR_INVALID_ITEM_ID);
                }

                // 보유 아이템 개수 validation
                int requestCnt = itemPositionDTO.getPositionList().size();
                if (requestCnt > findInventory.getCount()) {
                    throw new BaseException(OVER_ITEM_COUNT);
                }

                // 새로운 아이템 배치 저장
                HashSet<Position> positionHashSet = new HashSet<>();
                for (PositionDTO positionDTO : itemPositionDTO.getPositionList()) {
                    positionHashSet.add(new Position(positionDTO.getPosX(), positionDTO.getPosY()));
                }
                for (Position position : positionHashSet) {
                    findInventory.getItemPlacement().add(
                            Position.builder()
                                    .posX(position.getPosX())
                                    .posY(position.getPosY())
                                    .build());
                }

                saveInventory(findInventory);
            }
        } catch (BaseException e) {
            throw e;
        }

    }


    // 특정 사용자의 모든 인벤토리 데이터 조회
    public List<Inventory> findInventoryListByUser(User user) throws BaseException {
        try {
            return inventoryRepository.findByUser(user);
        } catch (Exception e) {
            log.error("findInventoryListByUser() : inventoryRepository.findByUser(user) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 인벤토리 count 조회
    public Integer findInventoryCount(Long userId, Long itemId) throws BaseException {
        try {
            return inventoryRepository.findInventoryCount(userId, itemId).orElse(0);
        } catch (Exception e) {
            log.error("findInventoryCount() : inventoryRepository.findInventoryCount(userId, itemId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }

    }

}
