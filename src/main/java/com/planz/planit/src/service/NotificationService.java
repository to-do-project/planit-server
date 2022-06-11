package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.goal.Goal;
import com.planz.planit.src.domain.notification.*;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.notification.NotificationLargeCategory.*;

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
     */
    public void createNotification(User user, NotificationSmallCategory category, String content, Goal goal) throws BaseException{
        try{

            if(user == null || category == null || content == null){
                throw new NullPointerException("user, category, content를 모두 입력해주세요.");
            }

            // 그룹 초대 요청 알림인 경우
            if(category == NotificationSmallCategory.GROUP_REQUEST){
                if (goal == null){
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
            // 그 외 알림인 경우
            else{
                Notification notification = Notification.builder()
                        .user(user)
                        .category(category)
                        .content(content)
                        .build();

                saveNotification(notification);
            }


            // 푸쉬 알림 보내기
            if(category.getLargeCategory() == NOTICE){

            }
            else if(category.getLargeCategory() == FRIEND){

            }
            else if(category.getLargeCategory() == PRIVATE){

            }
            else {

            }

        }
        catch (BaseException e){
            throw e;
        }
    }


    /**
     * Notification 엔티티 저장 혹은 업데이트
     */
    public void saveNotification(Notification notificationEntity) throws BaseException {
        try{
            notificationRepository.save(notificationEntity);
        }
        catch (Exception e){
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
    public void readNotification(Long notificationId, Long userId) throws BaseException{

        try{
            // 1. notificationId와 userId로 Notification 엔티티 조회
            Notification notification = findByNotificationIdAndUserId(notificationId, userId);

            // 2. 조회한 Notification의 readStatus를 읽음 상태(READ)로 변경
            notification.setReadStatus(NotificationStatus.READ);
            saveNotification(notification);
        }
        catch (BaseException e){
            throw e;
        }
    }

    /**
     * 그룹 초대 요청 알림에 대한 확정 처리
     * 1. userId, goalId로 Notification 엔티티 조회
     * 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
     */
    public void confirmNotification(Long userId, Long goalId) throws BaseException{
        try{
            // 1. userId, goalId로 Notification 엔티티 조회
            GroupReqNotification notification = findByUserIdAndGoalId(userId, goalId);

            // 2. 조회한 Notification의 confirmStatus를 확정 상태(CONFIRM)로 변경
            notification.setConfirmStatus(NotificationStatus.CONFIRM);
            saveNotification(notification);
        }
        catch (BaseException e){
            throw e;
        }
    }

    /**
     * notificationId와 userId로 DB에서 Notification 엔티티 조회
     */
    public Notification findByNotificationIdAndUserId(Long notificationId, Long userId) throws BaseException{
        try{
            return (Notification) notificationRepository.findByNotificationIdAndUserId(notificationId, userId).orElseThrow(()->new BaseException(INVALID_USER_ID_NOTIFICATION_ID));
        }
        catch (BaseException e){
            throw e;
        }
        catch (Throwable e){
            log.error("findByNotificationIdAndUserId(): notificationRepository.findByNotificationIdAndUserId() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * userId와 goalId로 DB에서 GroupReqNotification 엔티티 조회
     */
    public GroupReqNotification findByUserIdAndGoalId(Long userId, Long goalId) throws BaseException{
        try{
            return (GroupReqNotification) notificationRepository.findByUserIdAndGoalId(userId, goalId).orElseThrow(()->new BaseException(INVALID_USER_ID_GOAL_ID));
        }
        catch (BaseException e){
            throw e;
        }
        catch (Throwable e){
            log.error("findByUserIdAndGoalId(): notificationRepository.findByNotificationIdAndUserId() 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
