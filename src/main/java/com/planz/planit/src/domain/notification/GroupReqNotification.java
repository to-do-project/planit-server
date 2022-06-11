package com.planz.planit.src.domain.notification;

import com.planz.planit.src.domain.goal.Goal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GroupReqNotification extends Notification{ // 그룹 목표 초대 알림인 경우

    // 목표
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    // 그룹 초대 요청에 대한 수락 or 거절 확정 여부
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(name = "confirm_status")
    private NotificationStatus confirmStatus = NotificationStatus.NOT_CONFIRM;

    public void setConfirmStatus(NotificationStatus confirmStatus) {
        this.confirmStatus = confirmStatus;
    }
}
