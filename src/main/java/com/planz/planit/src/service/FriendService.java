package com.planz.planit.src.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.api.services.storage.Storage;
import com.planz.planit.config.BaseException;
import com.planz.planit.config.fcm.FirebaseCloudMessageService;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.friend.FriendRepository;
import com.planz.planit.src.domain.friend.FriendStatus;
import com.planz.planit.src.domain.friend.dto.AcceptReqDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendListResDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendResDTO;
import com.planz.planit.src.domain.notification.NotificationSmallCategory;
import com.planz.planit.src.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.friend.FriendStatus.FRIEND;
import static com.planz.planit.src.domain.friend.FriendStatus.WAIT;

@Slf4j
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DeviceTokenService deviceTokenService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    public FriendService(FriendRepository friendRepository, NotificationService notificationService, UserService userService, DeviceTokenService deviceTokenService, FirebaseCloudMessageService firebaseCloudMessageService) {
        this.friendRepository = friendRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.deviceTokenService = deviceTokenService;
        this.firebaseCloudMessageService = firebaseCloudMessageService;
    }

    //친구요청
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void save(long userId, long toUserId) throws BaseException {
        //엔티티 가져오기- 에러처리
        //탈퇴한 유저 - 데이터 없으면 서버에러 처리
        User user = userService.findUser(userId);
        User toUser = userService.findUser(toUserId);
        //이미 관계가 존재하는지 확인하기
        if (isFriend(user, toUser)) {
            throw new BaseException(ALREADY_EXIST_FRIEND);
        }
        //이미 요청한 친구인지 확인
        if (isWaitFriend(user, toUser)) {
            throw new BaseException(ALREADY_WAIT_FRIEND);
        }
        try {
            //friend 객체 만들어주기
            Friend friend = Friend.builder()
                    .fromUser(user)
                    .toUser(toUser)
                    .build();
            Friend save = friendRepository.save(friend);

            // 친구 요청 알림 보내기 -> 친구 요청을 받은 사용자(toUser)에게 전송
            notificationService.createNotification(toUser, NotificationSmallCategory.FRIEND_REQUEST, user.getNickname() + "님이 친구 요청을 했습니다.", friend, null, null);

            // FCM 보내기
            try {
                List<String> deviceTokens = deviceTokenService.findAllDeviceTokens_friendFlag1(toUserId);
                firebaseCloudMessageService.sendMessageTo(deviceTokens, "[친구 요청]", "새로운 친구요청이 왔습니다. 빨리 행성을 확인해주세요!");
                log.info("[FCM 전송 성공] friend_flag가 1인 특정 사용자에게, 친구 요청 FCM 전송 성공");
            }
            catch (BaseException e){
                log.error("[FCM 전송 실패] " + e.getStatus());
            }

        } catch (Exception e) {
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
            List<GetFriendResDTO> waitResult = new ArrayList<>();
            List<GetFriendResDTO> result = new ArrayList<>();
            for (Friend friend : byToUserId) {
                if (friend.getFriendStatus() == WAIT) {
                    waitResult.add(new GetFriendResDTO(friend.getFriendId(), friend.getFromUser().getUserId(), friend.getFromUser().getNickname(), friend.getFromUser().getProfileColor().toString(), true));
                } else {
                    result.add(new GetFriendResDTO(friend.getFriendId(), friend.getFromUser().getUserId(), friend.getFromUser().getNickname(), friend.getFromUser().getProfileColor().toString(), false));
                }
            }
            for (Friend friend : byFromUserId) {
                if (friend.getFriendStatus() == FRIEND) {
                    result.add(new GetFriendResDTO(friend.getFriendId(), friend.getToUser().getUserId(), friend.getToUser().getNickname(), friend.getToUser().getProfileColor().toString(), friend.getFriendStatus() == WAIT ? true : false));
                }
            }
            GetFriendListResDTO getFriendListResDTO = new GetFriendListResDTO(waitResult, result);
            return getFriendListResDTO;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //친구 수락
    //친구 거절 및 삭제
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public boolean acceptFriend(AcceptReqDTO acceptReqDTO) throws BaseException {
        Friend friend = friendRepository.findById(acceptReqDTO.getFriendId()).orElseThrow(() -> new BaseException(NOT_EXIST_FRIEND));
        try {
            friend.acceptFriend(acceptReqDTO.isAccepted()); //수락 혹은 거절로 변경
            friendRepository.save(friend);

            //친구 요청 알림에 대한 확정 처리
            notificationService.confirmFriendReqNotification(friend.getToUser().getUserId(), friend.getFriendId());

            if (acceptReqDTO.isAccepted()) {
                //친구 수락 알림 보내기 -> 친구 요청을 한 사용자(fromUser)에게 전송
                notificationService.createNotification(friend.getFromUser(), NotificationSmallCategory.FRIEND_ACCEPT, friend.getToUser().getNickname() + "님이 친구 수락을 했습니다.", null, null, null);

                // FCM 보내기
                try {
                    List<String> deviceTokens = deviceTokenService.findAllDeviceTokens_friendFlag1(friend.getFromUser().getUserId());
                    firebaseCloudMessageService.sendMessageTo(deviceTokens, "[친구 수락]", "친구 수락이 되었습니다. 행성을 확인해주세요!");
                    log.info("[FCM 전송 성공] friend_flag가 1인 특정 사용자에게, 친구 수락 FCM 전송 성공");
                } catch (BaseException e) {
                    log.error("[FCM 전송 실패] " + e.getStatus());
                }
            }

        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        return true;
    }

    // 서로 친구 관계인지 확인 - 혜지 추가
    public boolean isFriend(User myUser, User friendUser) throws BaseException {
        try {
            boolean result1 = friendRepository.existsByFromUserAndToUserAndFriendStatus(myUser, friendUser, FriendStatus.FRIEND);
            boolean result2 = friendRepository.existsByFromUserAndToUserAndFriendStatus(friendUser, myUser, FriendStatus.FRIEND);
            return (result1 | result2);
        } catch (Exception e) {
            log.error("isFriend() : friendRepository.existsByFromUserAndToUserAndFriendStatus() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 친구 요청 상태인지 확인
    public boolean isWaitFriend(User myUser, User friendUser) throws BaseException {
        try {
            boolean result1 = friendRepository.existsByFromUserAndToUserAndFriendStatus(myUser, friendUser, WAIT);
            boolean result2 = friendRepository.existsByFromUserAndToUserAndFriendStatus(friendUser, myUser, WAIT);
            return (result1 | result2);
        } catch (Exception e) {
            log.error("isFriend() : friendRepository.existsByFromUserAndToUserAndFriendStatus() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //닉네임으로 유저 검색
    public List<User> getSearchFriends(Long userId, String nickname) throws BaseException {

        List<Friend> toFriends = friendRepository.seachFriendsByToUser(userId, nickname);
        List<Friend> fromFriends = friendRepository.seachFriendsByFromUser(userId, nickname);

        if (toFriends.isEmpty() && fromFriends.isEmpty()) {
            throw new BaseException(NOT_EXIST_FRIEND);
        }
        List<User> fromUserList = toFriends.stream().map(s -> s.getFromUser()).collect(Collectors.toList());
        List<User> toUserList = fromFriends.stream().map(s -> s.getToUser()).collect(Collectors.toList());

        fromUserList.addAll(toUserList);

        return fromUserList;
    }
}
