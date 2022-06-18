package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.goal.Goal;
import com.planz.planit.src.domain.notice.Notice;
import com.planz.planit.src.domain.notification.*;
import com.planz.planit.src.domain.notification.dto.GetNotificationsResDTO;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.notification.NotificationLargeCategory.*;
import static com.planz.planit.src.domain.notification.NotificationSmallCategory.*;

@Service
@Log4j2
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 알림 생성 함수
     * 1. 공지사항 알림인 경우, NoticeNotification 엔티티 생성후 DB에 저장
     * 2. 친구 요청 알림인 경우, FriendReqNotification 엔티티 생성후 DB에 저장
     * 3. 그룹 초대 요청 알림인 경우, GroupReqNotification 엔티티 생성후 DB에 저장
     * 4. 그 외 알림인 경우, Notification 엔티티 생성후 DB에 저장
     * 5. 푸쉬 알림 보내기 => 추가 로직 필요!!!!
     */
    public void createNotification(User user, NotificationSmallCategory category, String content, Friend friend, Goal goal, Notice notice) throws BaseException {
        try {

            if (user == null || category == null || content == null) {
                throw new NullPointerException("user, category, content를 모두 입력해주세요.");
            }

            // 1. 공지사항 알림인 경우, NoticeNotification 엔티티 생성후 DB에 저장
            if (category == NOTICE_TWO){
                if (notice == null){
                    throw new NullPointerException("notice를 입력해주세요.");
                }

                NoticeNotification notification = NoticeNotification.builder()
                        .user(user)
                        .category(category)
                        .content(content)
                        .notice(notice)
                        .build();
                saveNotification(notification);
            }
            // 2. 친구 요청 알림인 경우, FriendReqNotification 엔티티 생성후 DB에 저장
            else if (category == FRIEND_REQUEST){
                if (friend == null) {
                    throw new NullPointerException("friend를 입력해주세요.");
                }

                FriendReqNotification notification = FriendReqNotification.builder()
                        .user(user)
                        .category(category)
                        .content(content)
                        .friend(friend)
                        .build();
                saveNotification(notification);
            }
            // 3. 그룹 초대 요청 알림인 경우, GroupReqNotification 엔티티 생성후 DB에 저장
            else if (category == GROUP_REQUEST) {
                if (goal == null) {
                    throw new NullPointerException("goal을 입력해주세요.");
                }

                GroupReqNotification notification = GroupReqNotification.builder()
                        .user(user)
                        .category(category)
                        .content(content)
                        .goal(goal)
                        .build();
                saveNotification(notification);
            }
            // 4. 그 외 알림인 경우, Notification 엔티티 생성후 DB에 저장
            else {
                Notification notification = Notification.builder()
                        .user(user)
                        .category(category)
                        .content(content)
                        .build();

                saveNotification(notification);
            }


            // 5. 푸쉬 알림 보내기
            if (category.getLargeCategory() == NOTICE) {

            } else if (category.getLargeCategory() == FRIEND) {

            } else if (category.getLargeCategory() == PRIVATE) {

            } else {

            }

        } catch (BaseException e) {
            throw e;
        }
    }


    /**
     * Notification 엔티티 저장 혹은 업데이트
     */
    public void saveNotification(Notification notificationEntity) throws BaseException {
        try {
            notificationRepository.save(notificationEntity);
        } catch (Exception e) {
            log.error("saveNotification() : notificationRepository.save(notificationEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 알림 읽음 처리 API
     * 1. notificationId와 userId로 Notification 엔티티 조회
     * 2. 조회한 Notification의 readStatus를 읽음 상태(READ)로 변경
     */
    public void readNotification(Long notificationId, Long userId) throws BaseException {

        try {
            // 1. notificationId와 userId로 Notification 엔티티 조회
            Notification notification = findByNotificationIdAndUserId(notificationId, userId);

            // 2. 조회한 Notification의 readStatus를 읽음 상태(READ)로 변경
            notification.setReadStatus(NotificationStatus.READ);
            saveNotification(notification);
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * 그룹 초대 요청 알림에 대한 확정 처리
     * 1. userId, goalId로 Notification 엔티티 조회
     * 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
     */
    public void confirmGroupReqNotification(Long userId, Long goalId) throws BaseException {
        try {
            // 1. userId, goalId로 Notification 엔티티 조회
            GroupReqNotification notification = findByUserIdAndGoalId(userId, goalId);
            // 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
            notification.setConfirmStatus(NotificationStatus.CONFIRM);
            saveNotification(notification);
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * 친구 요청 알림에 대한 확정 처리
     * 1. userId, friendId로 Notification 엔티티 조회
     * 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
     */
    public void confirmFriendReqNotification(Long userId, Long friendId) throws BaseException {
        try {
            // 1. userId, friendId로 Notification 엔티티 조회
            FriendReqNotification notification = findByUserIdAndFriendId(userId, friendId);
            // 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
            notification.setConfirmStatus(NotificationStatus.CONFIRM);
            saveNotification(notification);
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * notificationId와 userId로 DB에서 Notification 엔티티 조회
     */
    public Notification findByNotificationIdAndUserId(Long notificationId, Long userId) throws BaseException {
        try {
            return (Notification) notificationRepository.findByNotificationIdAndUserId(notificationId, userId).orElseThrow(() -> new BaseException(INVALID_USER_ID_NOTIFICATION_ID));
        } catch (BaseException e) {
            throw e;
        } catch (Throwable e) {
            log.error("findByNotificationIdAndUserId(): notificationRepository.findByNotificationIdAndUserId() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId와 goalId로 DB에서 GroupReqNotification 엔티티 조회
     */
    public GroupReqNotification findByUserIdAndGoalId(Long userId, Long goalId) throws BaseException {
        try {
            return (GroupReqNotification) notificationRepository.findByUserIdAndGoalId(userId, goalId).orElseThrow(() -> new BaseException(INVALID_USER_ID_GOAL_ID));
        } catch (BaseException e) {
            throw e;
        } catch (Throwable e) {
            log.error("findByUserIdAndGoalId(): notificationRepository.findByUserIdAndGoalId() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId와 friendId로 DB에서 FriendReqNotification 엔티티 조회
     */
    public FriendReqNotification findByUserIdAndFriendId(Long userId, Long friendId) throws BaseException {
        try {
            return (FriendReqNotification) notificationRepository.findByUserIdAndFriendId(userId, friendId).orElseThrow(() -> new BaseException(INVALID_USER_ID_FRIEND_ID));
        } catch (BaseException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            log.error("findByUserIdAndFriendId(): notificationRepository.findByUserIdAndFriendId() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 알림 조회 API
     * - 공지사항 2개 최상단
     * - 확정하지 않은 친구 요청, 그룹 초대 요청은 최상단
     * - 나머지 알림은 시간순으로 최대 200개까지 조회 가능
     * 1. 확정하지 않은 그룹 초대 요청 알림 리스트 조회
     * 2. 확정하지 않은 친구 요청 알림 리스트 조회
     * 3. 공지사항 알림 리스트 조회 (최대 2개)
     * 4. 일반 알림 리스트 조회
     */
    public GetNotificationsResDTO getNotifications(Long userId) throws BaseException {

        try {

            // 1. 확정하지 않은 그룹 초대 요청 알림 리스트 조회
            List<GroupReqNotification> groupReqNotifications = getAllNotConfirmedGroupReqNotis(userId);
            List<GetNotificationsResDTO.GroupReqNotificationDTO> groupReqNotificationsResult = groupReqNotifications.stream().
                    map(n -> GetNotificationsResDTO.GroupReqNotificationDTO.builder()
                            .notificationId(n.getNotificationId())
                            .userId(n.getUser().getUserId())
                            .category(n.getCategory().name())
                            .content(n.getContent())
                            .createAt(n.getCreateAt())
                            .readStatus(n.getReadStatus().name())
                            .goalId(n.getGoal().getGoalId())
                            .confirmStatus(n.getConfirmStatus().name())
                            .build())
                    .sorted(Comparator.comparing(GetNotificationsResDTO.GroupReqNotificationDTO::getCreateAt).reversed())
                    .collect(Collectors.toList());

            // 2. 확정하지 않은 친구 요청 알림 리스트 조회
            List<FriendReqNotification> friendReqNotifications = getAllNotConfirmedFriendReqNotis(userId);
            List<GetNotificationsResDTO.FriendReqNotificationDTO> friendReqNotificationsResult = friendReqNotifications.stream()
                    .map(n -> GetNotificationsResDTO.FriendReqNotificationDTO.builder()
                            .notificationId(n.getNotificationId())
                            .userId(n.getUser().getUserId())
                            .category(n.getCategory().name())
                            .content(n.getContent())
                            .createAt(n.getCreateAt())
                            .readStatus(n.getReadStatus().name())
                            .friendId(n.getFriend().getFriendId())
                            .confirmStatus(n.getConfirmStatus().name())
                            .build())
                    .sorted(Comparator.comparing(GetNotificationsResDTO.FriendReqNotificationDTO::getCreateAt).reversed())
                    .collect(Collectors.toList());


            // 3. 공지사항 알림 리스트 조회 (최대 2개)
            List<NoticeNotification> noticeNotifications = getAllNoticeNotifications(userId);
            List<GetNotificationsResDTO.NoticeNotificationDTO> noticeNotificationsResult =
                    noticeNotifications.stream()
                            .filter(n -> n.getCategory() == NOTICE_TWO)
                            .map(n -> GetNotificationsResDTO.NoticeNotificationDTO.builder()
                                    .notificationId(n.getNotificationId())
                                    .userId(n.getUser().getUserId())
                                    .category(n.getCategory().name())
                                    .content(n.getContent())
                                    .createAt(n.getCreateAt())
                                    .readStatus(n.getReadStatus().name())
                                    .noticeId(n.getNotice().getNoticeId())
                                    .build())
                            .sorted(Comparator.comparing(GetNotificationsResDTO.NoticeNotificationDTO::getCreateAt).reversed())
                            .limit(2)
                            .collect(Collectors.toList());

            // 4. 일반 알림 리스트 조회
            List<Notification> etcNotifications = getAllNotifications(userId);
            List<GetNotificationsResDTO.BasicNotificationDTO> etcNotificationsResult =
                    etcNotifications.stream()
                            .filter(n -> n.getCategory() != NOTICE_TWO && n.getCategory() != FRIEND_REQUEST && n.getCategory() != GROUP_REQUEST)
                            .map(n -> GetNotificationsResDTO.BasicNotificationDTO.builder()
                                    .notificationId(n.getNotificationId())
                                    .userId(n.getUser().getUserId())
                                    .category(n.getCategory().name())
                                    .content(n.getContent())
                                    .createAt(n.getCreateAt())
                                    .readStatus(n.getReadStatus().name())
                                    .build())
                            .sorted(Comparator.comparing(GetNotificationsResDTO.BasicNotificationDTO::getCreateAt).reversed())
                            .limit(200 - noticeNotificationsResult.size() - friendReqNotificationsResult.size() - groupReqNotificationsResult.size())
                            .collect(Collectors.toList());

            return GetNotificationsResDTO.builder()
                    .noticeNotifications(noticeNotificationsResult)
                    .friendReqNotifications(friendReqNotificationsResult)
                    .groupReqNotifications(groupReqNotificationsResult)
                    .etcNotifications(etcNotificationsResult)
                    .build();

        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * userId로 DB에서 모든 확정되지 않은 GroupReqNotification 엔티티 조회
     */
    public List<GroupReqNotification> getAllNotConfirmedGroupReqNotis(Long userId) throws BaseException {
        try {
            return notificationRepository.getAllNotConfirmedGroupReqNotis(userId, NotificationStatus.NOT_CONFIRM);
        } catch (Exception e) {
            log.error("getAllNotConfirmedGroupReqNotis() : notificationRepository.getAllNotConfirmedGroupReqNotis() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId로 DB에서 모든 확정되지 않은 FriendReqNotification 엔티티 조회
     */
    public List<FriendReqNotification> getAllNotConfirmedFriendReqNotis(Long userId) throws BaseException {
        try {
            return notificationRepository.getAllNotConfirmedFriendReqNotis(userId, NotificationStatus.NOT_CONFIRM);
        } catch (Exception e) {
            log.error("getAllNotConfirmedFriendReqNotis() : notificationRepository.getAllNotConfirmedFriendReqNotis() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId로 DB에서 모든 NoticeNotification 엔티티 조회
     */
    public List<NoticeNotification> getAllNoticeNotifications(Long userId) throws BaseException {
        try {
            return notificationRepository.getAllNoticeNotifications(userId);
        } catch (Exception e) {
            log.error("getAllNoticeNotifications() : notificationRepository.getAllNoticeNotifications(userId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId로 DB에서 모든 Notification 엔티티 조회
     */
    public List<Notification> getAllNotifications(Long userId) throws BaseException {
        try {
            return notificationRepository.getAllNotifications(userId);
        } catch (Exception e) {
            log.error("getAllNotifications() : notificationRepository.getAllNotifications(userId) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
