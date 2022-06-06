package com.planz.planit.src.domain.todo;

import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Entity
@NoArgsConstructor
public class TodoMemberLike {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="todo_like_id")
    private Long todoLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="todo_member_id")
    private TodoMember todoMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;//누른 사람

    @Column(name="create_at")
    private LocalDateTime createAt = now();

    @Builder
    public TodoMemberLike(TodoMember todoMember,User user){
        this.todoMember=todoMember;
        this.user = user;
    }
}
