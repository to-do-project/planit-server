package com.planz.planit.src.domain.notification;

import com.planz.planit.src.domain.friend.Friend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity

@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendReqNotification extends Notification { // 친구 초대 알림인 경우

    // 친구
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    private Friend friend;

    // 친구 초대 요청에 대한 수락 or 거절 확정 여부
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(name = "confirm_status")
    private NotificationStatus confirmStatus = NotificationStatus.NOT_CONFIRM;

    public void setConfirmStatus(NotificationStatus confirmStatus) {
        this.confirmStatus = confirmStatus;
    }


}
