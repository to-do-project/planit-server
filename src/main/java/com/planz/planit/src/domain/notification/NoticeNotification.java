package com.planz.planit.src.domain.notification;

import com.planz.planit.src.domain.notice.Notice;
import com.planz.planit.src.domain.notification.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@SuperBuilder

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeNotification extends Notification {  // 공지사항 알림인 경우

    // 공지사항 아이디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;
}
