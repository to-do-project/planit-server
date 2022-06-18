package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.friend.FriendRepository;
import com.planz.planit.src.domain.friend.FriendStatus;
import com.planz.planit.src.domain.friend.dto.AcceptReqDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendListResDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendResDTO;
import com.planz.planit.src.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.friend.FriendStatus.WAIT;

@Slf4j
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public FriendService(FriendRepository friendRepository, NotificationService notificationService, UserService userService) {
        this.friendRepository = friendRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    //친구요청
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void save(long userId, long toUserId) throws BaseException {
        //엔티티 가져오기- 에러처리
        //탈퇴한 유저 - 데이터 없으면 서버에러 처리
        User user = userService.findUser(userId);
        User toUser = userService.findUser(toUserId);
        //이미 관계가 존재하는지 확인하기
        if(isFriend(user,toUser)){
            throw new BaseException(ALREADY_EXIST_FRIEND);
        }
        //이미 요청한 친구인지 확인
        if(isWaitFriend(user,toUser)){
            throw new BaseException(ALREADY_WAIT_FRIEND);
        }
        try {
            //friend 객체 만들어주기
            Friend friend = Friend.builder()
                    .fromUser(user)
                    .toUser(toUser)
                    .build();
            friendRepository.save(friend);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }


    }

    //유저의 친구목록 조회
    public GetFriendListResDTO getFriends(Long userId) throws BaseException {
        try {
            //WAIT, FRIEND 상태인 친구만 불러오도록 함 => WAIT 조건을 아예 걸어버리기
            List<Friend> byFromUserId = friendRepository.findFriendByFromUser(userId);
            List<Friend> byToUserId = friendRepository.findByToUser(userId);
            //데이터 정제하기 (상대방만 출력되게)
            List<GetFriendResDTO> result = new ArrayList<>();
            for (Friend friend : byToUserId) {
                result.add(new GetFriendResDTO(friend.getFriendId(), friend.getFromUser().getUserId(),friend.getFromUser().getNickname(),friend.getFromUser().getProfileColor().toString(),friend.getFriendStatus()==WAIT?true:false));
            }
            for (Friend friend : byFromUserId) {
                result.add(new GetFriendResDTO(friend.getFriendId(),friend.getToUser().getUserId(),friend.getToUser().getNickname(),friend.getToUser().getProfileColor().toString(),friend.getFriendStatus()==WAIT?true:false));
            }
            List<GetFriendResDTO> wait = result.stream().filter(m -> m.isWaitFlag()==true).collect(Collectors.toList());
            List<GetFriendResDTO> friends = result.stream().filter(m -> m.isWaitFlag()==false).collect(Collectors.toList());
            GetFriendListResDTO getFriendListResDTO = new GetFriendListResDTO(wait, friends);
            return getFriendListResDTO;
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //친구 수락
    //친구 거절 및 삭제
    public boolean acceptFriend(AcceptReqDTO acceptReqDTO) throws BaseException {
        try {
            Friend friend = friendRepository.findById(acceptReqDTO.getFriendId()).orElseThrow();
            friend.acceptFriend(acceptReqDTO.isAccepted()); //수락 혹은 거절로 변경
            friendRepository.save(friend);
            //알림 추가
            notificationService.confirmFriendReqNotification(friend.getToUser().getUserId(), friend.getFriendId());
            return true;
        } catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
    }
    }

    // 서로 친구 관계인지 확인 - 혜지 추가
    public boolean isFriend(User myUser, User friendUser) throws BaseException{
        try {
            boolean result1 = friendRepository.existsByFromUserAndToUserAndFriendStatus(myUser, friendUser, FriendStatus.FRIEND);
            boolean result2 = friendRepository.existsByFromUserAndToUserAndFriendStatus(friendUser, myUser, FriendStatus.FRIEND);
            return (result1 | result2);
        }
        catch (Exception e){
            log.error("isFriend() : friendRepository.existsByFromUserAndToUserAndFriendStatus() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 친구 요청 상태인지 확인
    public boolean isWaitFriend(User myUser, User friendUser) throws BaseException{
        try {
            boolean result1 = friendRepository.existsByFromUserAndToUserAndFriendStatus(myUser, friendUser, WAIT);
            boolean result2 = friendRepository.existsByFromUserAndToUserAndFriendStatus(friendUser, myUser, WAIT);
            return (result1 | result2);
        }
        catch (Exception e){
            log.error("isFriend() : friendRepository.existsByFromUserAndToUserAndFriendStatus() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
