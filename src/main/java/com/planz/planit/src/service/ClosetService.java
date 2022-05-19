package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.closet.Closet;
import com.planz.planit.src.domain.closet.ClosetRepository;
import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;

@Service
@Log4j2
public class ClosetService {

    private final ClosetRepository closetRepository;

    @Autowired
    public ClosetService(ClosetRepository closetRepository) {
        this.closetRepository = closetRepository;
    }

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
}
