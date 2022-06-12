package com.planz.planit.src.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository<T extends Notification> extends JpaRepository<T, Long> {

    @Query(value = "select n from Notification n where n.notificationId = :notificationId and n.user.userId = :userId")
    Optional<Notification> findByNotificationIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Query(value = "select grn from GroupReqNotification grn where grn.user.userId = :userId and grn.goal.goalId = :goalId")
    Optional<GroupReqNotification> findByUserIdAndGoalId(@Param("userId") Long userId, @Param("goalId") Long goalId);

    @Query(value = "select frn from FriendReqNotification frn where frn.user.userId = :userId and frn.friend.friendId = :friendId")
    Optional<FriendReqNotification> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query(value = "select grn from GroupReqNotification grn where grn.user.userId = :userId and grn.confirmStatus = :confirmStatus")
    List<GroupReqNotification> getAllNotConfirmedGroupReqNotis(@Param("userId") Long userId, @Param("confirmStatus") NotificationStatus confirmStatus);

    @Query(value = "select frn from FriendReqNotification frn where frn.user.userId = :userId and frn.confirmStatus = :confirmStatus")
    List<FriendReqNotification> getAllNotConfirmedFriendReqNotis(@Param("userId") Long userId, @Param("confirmStatus") NotificationStatus confirmStatus);

    @Query(value = "select n from Notification n where n.user.userId = :userId")
    List<Notification> getAllNotifications(@Param("userId") Long userId);
}
