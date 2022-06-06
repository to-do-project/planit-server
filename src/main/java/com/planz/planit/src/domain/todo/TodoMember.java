package com.planz.planit.src.domain.todo;

import com.planz.planit.src.domain.goal.Goal;
import com.planz.planit.src.domain.goal.GoalMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Getter
@Entity
@NoArgsConstructor
public class TodoMember {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="todo_member_id")
    private Long todoMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="todo_id")
    private Todo todo;

    @ManyToOne
    @JoinColumn(name="goal_member_id")
    private GoalMember goalMember;

    @Enumerated(EnumType.STRING)
    @Column(name="complete_flag")
    private CompleteFlag completeFlag = CompleteFlag.INCOMPLETE;

    @Column(name="update_at")
    private LocalDateTime updateAt = now();

    //양방향 mapping
    @OneToMany(mappedBy = "todoMember",fetch = FetchType.LAZY)
    private List<TodoMemberLike> todoMemberLikes = new ArrayList<>();


    @Builder
    public TodoMember(Todo todo, GoalMember goalMember){
        this.todo = todo;
        this.goalMember = goalMember;
    }

    //checkTodo
    public void checkTodo(){
        this.completeFlag = CompleteFlag.COMPLETE;
        this.updateAt = now();
    }
    //uncheckTodo()
    public void uncheckTodo(){
        this.completeFlag = CompleteFlag.INCOMPLETE;
        this.updateAt = now();
    }

    @Override
    public String toString() {
        return "todoMemberId"+this.todoMemberId+
                "\n completeFlag"+this.completeFlag;
    }
}
