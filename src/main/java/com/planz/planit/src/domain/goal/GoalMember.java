package com.planz.planit.src.domain.goal;

import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name="goalMember_uk",
                        columnNames = {"from_user_id", "to_user_id"}
                )
        }
)
public class GoalMember {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_member_id")
    private Long goalMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="goal_id")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status; //매니저의 경우 무조건 accept, 다른 멤버는 wait 상태

}
