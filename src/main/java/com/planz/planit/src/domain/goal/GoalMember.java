package com.planz.planit.src.domain.goal;

import com.planz.planit.src.domain.todo.TodoMember;
import com.planz.planit.src.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name="goalMember_uk",
                        columnNames = {"goal_id", "user_id"}
                )
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalMember {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_member_id")
    private Long goalMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="goal_id")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status; //매니저의 경우 무조건 accept, 다른 멤버는 wait 상태

    @Enumerated(EnumType.STRING)
    @Column(name="member_role", nullable=false)
    private GoalMemberRole memberRole;

    //양방향 mapping
    @OneToMany(mappedBy = "goalMember",fetch = FetchType.LAZY)
    private List<TodoMember> todoMembers = new ArrayList<>();



    public void accept(){
        this.status= GroupStatus.ACCEPT;
    }
}
