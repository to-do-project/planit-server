package com.planz.planit.src.domain.todo;

import com.planz.planit.src.domain.goal.Goal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Entity
@NoArgsConstructor
public class Todo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="todo_id")
    private Long todoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="goal_id")
    private Goal goal;

    @Column(nullable=false)
    private String title;

    @Column(name="create_at",nullable = false)
    private LocalDateTime createAt = now();

    @Builder
    public Todo(Goal goal, String title){
        this.goal = goal;
        this.title = title;
    }
}
