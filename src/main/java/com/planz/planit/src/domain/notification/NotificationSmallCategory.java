package com.planz.planit.src.domain.notification;

import lombok.Getter;

import static com.planz.planit.src.domain.notification.NotificationLargeCategory.*;

@Getter
public enum NotificationSmallCategory {

    NOTICE_TWO(NOTICE, "공지사항 알림"),

    FRIEND_REQUEST(FRIEND,"친구 요청"),
    FRIEND_ACCEPT(FRIEND,"친구 수락"),

    GROUP_REQUEST(GROUP, "그룹 초대 요청"),
    GROUP_ACCEPT(GROUP, "그룹 초대 수락"),
    GROUP_DONE(GROUP, "그룹 투두 완료"),

    TODO_FAVORITE(TODO,"투두 좋아요"),
    TODO_CHEER(TODO,"투두 응원");


    private final NotificationLargeCategory largeCategory;
    private final String kName;

    NotificationSmallCategory(NotificationLargeCategory largeCategory, String kName) {
        this.largeCategory = largeCategory;
        this.kName = kName;
    }
}
