package com.planz.planit.src.domain.goal;

import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.planz.planit.src.domain.goal.GoalStatus.ACTIVE;
import static com.planz.planit.src.domain.goal.GoalStatus.ARCHIVE;

@Entity
@Getter
@NoArgsConstructor
public class Goal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_id")
    private Long goalId;

    @Column(nullable=false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name="group_flag")
    private GroupCategory groupFlag;

    @Enumerated(EnumType.STRING)
    @Column(name="open_flag", nullable = false)
    private OpenCategory openFlag;

    @Enumerated(EnumType.STRING)
    @Column(name="goal_status",nullable = false)
    private GoalStatus goalStatus = ACTIVE;

    @Column(name="create_at",nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Builder
    public Goal(String title, GroupCategory groupFlag, OpenCategory openFlag){
        this.title = title;
        this.groupFlag = groupFlag;
        this.openFlag = openFlag;
    }

    public void modifyGoal(String title,OpenCategory openFlag){
        this.title=title;
        this.openFlag = openFlag;
    }

    public void archiveGoal(){
        this.goalStatus=ARCHIVE;
    }
    public void activateGoal(){
        this.goalStatus = ACTIVE;
    }

}
