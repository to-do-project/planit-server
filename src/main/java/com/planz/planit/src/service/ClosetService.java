package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.closet.Closet;
import com.planz.planit.src.domain.closet.ClosetRepository;
import com.planz.planit.src.domain.closet.dto.GetClosetResDTO;
import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;
import static com.planz.planit.config.BaseResponseStatus.NOT_OWN_CHARACTER_ITEM;

@Service
@Log4j2
public class ClosetService {

    private final ClosetRepository closetRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public ClosetService(ClosetRepository closetRepository, UserService userService, ItemService itemService) {
        this.closetRepository = closetRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    // 해당 유저가 해당 캐릭터 아이템을 가지고 있는지 확인
    public boolean existsClosetByItemAndUser(Item item, User user) throws BaseException {
        try {
            return closetRepository.existsByItemAndUser(item, user);
        }
        catch (Exception e){
            log.error("existsClosetByItemAndUser() : closetRepository.existsByItemAndUser(item, user) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // closet 저장
    public void saveCloset(Closet closetEntity) throws BaseException {
        try{
            closetRepository.save(closetEntity);
        }
        catch (Exception e){
            log.error("saveCloset() : closetRepository.save(closetEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 보유 중인 캐릭터 아이템 목록을 반환한다.
     * @param userId
     * @return GetClosetResDTO
     * @throws BaseException
     */
    public GetClosetResDTO getCloset(Long userId) throws BaseException{
        try{
            GetClosetResDTO result = new GetClosetResDTO();

            // 유저가 가지고 있는 캐릭터 아이템 목록 조회
            User user = userService.findUser(userId);
            List<Closet> closetList = findClosetListByUser(user);

            for (Closet closet : closetList) {
                result.getCharacterItemIdList().add(closet.getItem().getItemId());
            }
            return result;
        }
        catch (BaseException e){
            throw e;
        }
    }

    public List<Closet> findClosetListByUser(User user) throws BaseException {
        try{
            return closetRepository.findByUser(user);
        }
        catch (Exception e){
            log.error("findClosetListByUser(): closetRepository.findByUser(user) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 보유한 캐릭터 아이템을 적용한다.
     * @param userId, itemId
     * @throws BaseException
     */
    public void applyCharacterItem(Long userId, Long itemId) throws BaseException {
        try{

            User user = userService.findUser(userId);
            Item item = itemService.findItemByItemId(itemId);

            // itemId에 대한 논리적 validation => 해당 캐릭터 아이템을 보유하고 있는지
            if(!existsClosetByItemAndUser(item, user)){
                throw new BaseException(NOT_OWN_CHARACTER_ITEM);
            }

            // 사용자가 현재 사용 중인 캐릭터 아이템 변경
            userService.changeCharacterItem(user, itemId);

        }
        catch (BaseException e){
            throw e;
        }
    }
}
