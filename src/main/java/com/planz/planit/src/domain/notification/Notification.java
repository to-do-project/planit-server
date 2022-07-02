package com.planz.planit.src.domain.notification;

import com.planz.planit.src.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 알림 종류
    @Enumerated(value = EnumType.STRING)
    private NotificationSmallCategory category;

    // 알림 내용
    private String content;

    // 알림 생성일
    @Builder.Default
    @Column(name = "create_at")
    private LocalDateTime createAt = LocalDateTime.now();

    // 읽음 여부
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(name = "read_status")
    private NotificationStatus readStatus = NotificationStatus.NOT_READ;

    public void setReadStatus(NotificationStatus readStatus) {
        this.readStatus = readStatus;
    }

}
