package com.planz.planit.src.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository<T extends Notification> extends JpaRepository<T, Long> {

    @Query(value = "select n from Notification n where n.notificationId = :notificationId and n.user.userId = :userId")
    Optional<Notification> findByNotificationIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Query(value = "select grn from GroupReqNotification grn where grn.user.userId = :userId and grn.goal.goalId = :goalId")
    Optional<GroupReqNotification> findByUserIdAndGoalId(@Param("userId") Long userId, @Param("goalId") Long goalId);
}
