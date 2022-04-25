package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.friend.FriendRepository;
import com.planz.planit.src.domain.friend.dto.AcceptReqDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendResDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;
import static com.planz.planit.config.BaseResponseStatus.SERVER_ERROR;

@Slf4j
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserService userService;

    public FriendService(FriendRepository friendRepository, UserService userService) {
        this.friendRepository = friendRepository;
        this.userService = userService;
    }

    //친구요청
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void save(long userId, long toUserId) throws BaseException {
        //엔티티 가져오기- 에러처리
        try {
            //탈퇴한 유저 - 데이터 없으면 서버에러 처리
            User user = userService.findUser(userId);
            User toUser = userService.findUser(toUserId);
            //이미 관계가 존재하는지 확인하기
            if(friendRepository.existsByFromUserIdAndToUserId(userId,toUserId)||friendRepository.existsByFromUserIdAndToUserId(toUserId,userId)){
                throw new BaseException(SERVER_ERROR);
            }
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
    public List<GetFriendResDTO> getFriends(Long userId) throws BaseException {
        try {
            //WAIT, FRIEND 상태인 친구만 불러오도록 함
            List<Friend> byFromUserId = friendRepository.findByFromUserId(userId);
            List<Friend> byToUserId = friendRepository.findByToUserId(userId);
            //데이터 정제하기 (상대방만 출력되게)
            List<GetFriendResDTO> result = new ArrayList<>();
            for (Friend friend : byToUserId) {
                result.add(new GetFriendResDTO(friend.getFromUser().getId(),friend.getFromUser().getNickname(),friend.getFriendStatus().toString()));
            }
            for (Friend friend : byFromUserId) {
                result.add(new GetFriendResDTO(friend.getToUser().getId(),friend.getToUser().getNickname(),friend.getFriendStatus().toString()));
            }
            return result;
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
            return true;
        } catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
    }
    }


}
