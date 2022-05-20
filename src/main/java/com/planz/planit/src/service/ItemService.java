package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.closet.Closet;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.item.ItemRepository;
import com.planz.planit.src.domain.item.ItemType;
import com.planz.planit.src.domain.item.dto.BuyItemResDTO;
import com.planz.planit.src.domain.item.dto.GetItemInfoResDTO;
import com.planz.planit.src.domain.item.dto.GetItemStoreInfoResDTO;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final InventoryService inventoryService;
    private final ClosetService closetService;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserService userService, @Lazy InventoryService inventoryService, @Lazy ClosetService closetService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.closetService = closetService;
    }


/*    public Item findPlanetItemById(Long itemId) throws BaseException {

        return itemRepository.findItemByIdAndType(itemId, ItemType.PLANET_ITEM).orElseThrow(
                () -> {
                    log.error("findPlanetItemById() : itemRepository.findItemByIdAndType() 실행 중 데이터베이스 에러 발생헸거나, " +
                            "입력으로 들어온 itemId에 해당하는 행성 아이템이 존재하지 않음");
                    return new BaseException(INVALID_PLANET_ITEM_ID);
                }
        );
    }*/


    /** 아이템 스토어 전체 목록 조회 API
     * 아이템 스토어 전체 화면에서 필요한 모든 정보를 넘겨준다.
     * => 닉네임, 보유 포인트, 캐릭터 아이템 아이디 리스트, 행성 아이템 아이디 리스트 반환
     * @param userId
     * @return GetItemStoreInfoResDTO
     * @throws BaseException
     */
    public GetItemStoreInfoResDTO getItemStoreInfo(Long userId) throws BaseException{

        try{
            // 1. 유저 정보 조회 => 존재하지 않는 유저이면 BaseException throw
            User user = userService.findUser(userId);

            // 2. 아이템 목록 조회
            List<Long> characterItemIdList = findItemIdsByType(ItemType.CHARACTER_ITEM);
            List<Long> planetItemIdList = findItemIdsByType(ItemType.PLANET_ITEM);

            // 3. GetItemStoreInfoResDTO 반환
            return GetItemStoreInfoResDTO.builder()
                    .nickname(user.getNickname())
                    .point(user.getPoint())
                    .characterItemIdList(characterItemIdList)
                    .planetItemIdList(planetItemIdList)
                    .build();

        }
        catch (BaseException e){
            throw e;
        }
    }

    public List<Long> findItemIdsByType(ItemType type) throws BaseException {

        try{
            return itemRepository.findItemIdsByType(type);
        }
        catch (Exception e){
            log.error("findItemIdsByType() : itemRepository.findItemIdsByType(type) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 아이템 스토어에서 특정 아이템의 세부 정보를 넘겨준다.
     * => 아이템 아이디, 아이템 이름, 아이템 설명, 아이템 가격, 아이템 종류, 구매할 수 있는 최소 & 최대 수량 반환
     * @param itemId
     * @return GetItemInfoResDTO
     * @throws BaseException
     */
    public GetItemInfoResDTO getItemInfo(Long userId, Long itemId) throws BaseException{

        try{
            // 1. 아이템 정보 조회 => 존재하지 않는 아이템이면 BaseException throw
            Item item = findItemByItemId(itemId);

            // 2. 유저 정보 조회 => 존재하지 않는 아이템이면 BaseException throw
            User user = userService.findUser(userId);

            // 3. maxCnt, minCnt 계산
            int maxCnt = calculateMaxCnt(user, item);
            int minCnt = 1;
            if (maxCnt == 0){
                minCnt = 0;
            }


            // 4. GetItemInfoResDTO 반환
            return GetItemInfoResDTO.builder()
                    .itemId(itemId)
                    .name(item.getName())
                    .description(item.getDescription())
                    .price(item.getPrice())
                    .maxCnt(maxCnt)
                    .minCnt(minCnt)
                    .type(item.getType().getKName())
                    .build();
        }
        catch (BaseException e){
            throw e;
        }
    }


    // 구매할 수 있는 최대 아이템 개수 계산
    public int calculateMaxCnt(User user, Item item) throws BaseException {

        int maxCnt = item.getMaxCnt();

        // 행성 아이템인 경우
        if (item.getType() == ItemType.PLANET_ITEM){    // enum 타입은 == 비교 가능
            Integer currentCnt = inventoryService.findInventoryCount(user.getUserId(), item.getItemId());
            maxCnt -= currentCnt;
            if (maxCnt < 0) {
                maxCnt = 0;
            }
        }
        // 캐릭터 아이템인 경우
        else{
            boolean isExist = closetService.existsClosetByItemAndUser(item, user);
            if (isExist){
                maxCnt = 0;
            }
        }

        return maxCnt;
    }

    public Item findItemByItemId(Long itemId) throws BaseException {
        try {
            return itemRepository.findById(itemId).orElseThrow(() -> new BaseException(INVALID_ITEM_ID));
        }
        catch (BaseException e){
            throw e;
        }
        catch (Exception e){
            log.error("findItemByItemId() : itemRepository.findById(itemId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 아이템 스토어에서 특정 아이템을 구매한다.
     * => 아이템 아이디, 업데이트된 구매할 수 있는 최소 & 최대 수량, 업데이트된 보유 포인트 반환
     * @param userId, itemId, count, totalPrice
     * @return BuyItemResDTO
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public BuyItemResDTO buyItem(Long userId, Long itemId, int count, int totalPrice) throws BaseException{
        try {

            // 1. 아이템 정보 조회
            Item item = findItemByItemId(itemId);

            // 2. totalPrice에 대한 논리적 validation => 지불할 가격이 잘 계산되었는지
            if (totalPrice != (item.getPrice() * count)){
                throw new BaseException(INVALID_ITEM_TOTAL_PRICE);
            }

            // 3. point에 대한 논리적 validation => 현재 보유중인 포인트가 부족하지 않은지
            User user = userService.findUser(userId);   // 존재하지 않는 유저이면 BaseException throw
            if (user.getPoint() < totalPrice) {
                throw new BaseException(LACK_OF_POINT);
            }

            // 4. count에 대한 논리적 validation => 아이템 개수는 maxCnt개 이상 보유할 수 없다.
            int maxCnt = calculateMaxCnt(user, item);
            if (maxCnt < count){
                throw new BaseException(MAX_ITEM_COUNT);
            }


            // 5-1. 행성 아이템 구매
            if(item.getType() == ItemType.PLANET_ITEM){
                Inventory inventory = inventoryService.findInventoryByUserAndItem(user, item);
                inventory.setCount(inventory.getCount() + count);

                // 새로운 인벤토리 데이터 생성 혹은 기존 count값 수정
                inventoryService.saveInventory(inventory);
            }
            // 5-2. 캐릭터 아이템 구매
            else{
                Closet closet = Closet.builder()
                        .user(user)
                        .item(item)
                        .build();

                // 새로운 클로젯 데이터 생성
                closetService.saveCloset(closet);
            }

            // 6. User의 point 갱신
            user.setPoint(user.getPoint() - totalPrice);
            userService.saveUser(user);

            // 7. BuyItemResDTO 반환
            maxCnt -= count;
            int minCnt = 1;
            if (maxCnt == 0){
                minCnt = 0;
            }

            return BuyItemResDTO.builder()
                    .itemId(itemId)
                    .minCnt(minCnt)
                    .maxCnt(maxCnt)
                    .point(user.getPoint())
                    .build();
        } catch (BaseException e) {
            throw e;
        }
    }
}
