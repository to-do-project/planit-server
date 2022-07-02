package com.planz.planit.src.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.planz.planit.src.domain.notification.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetNotificationsResDTO {

    // "공지사항" 알림 리스트
    List<NoticeNotificationDTO> noticeNotifications;

    // "친구 요청" 알림 리스트
    List<FriendReqNotificationDTO> friendReqNotifications;

    // "그룹 초대 요청" 알림 리스트
    List<GroupReqNotificationDTO> groupReqNotifications;

    // "그 외" 알림 리스트
    List<BasicNotificationDTO> etcNotifications;


    @Getter
    @Builder
    public static class NoticeNotificationDTO{
        private Long notificationId;
        private Long userId;
        private String category;
        private String content;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime createAt;

        private String readStatus;
        private Long noticeId;
    }

    @Getter
    @Builder
    public static class FriendReqNotificationDTO{
        private Long notificationId;
        private Long userId;
        private String category;
        private String content;

        // 자바 클래스 -> json 으로 serialize할 때 문제가 생기므로 추가
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime createAt;

        private String readStatus;
        private Long friendId;
        private String confirmStatus;
    }

    @Getter
    @Builder
    public static class GroupReqNotificationDTO{
        private Long notificationId;
        private Long userId;
        private String category;
        private String content;

        // 자바 클래스 -> json 으로 serialize할 때 문제가 생기므로 추가
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime createAt;

        private String readStatus;
        private Long goalId;
        private String confirmStatus;
    }

    @Getter
    @Builder
    public static class BasicNotificationDTO{
        private Long notificationId;
        private Long userId;
        private String category;
        private String content;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime createAt;

        private String readStatus;
    }
}
