package com.planz.planit.src.domain.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoMemberLikeRepository extends JpaRepository<TodoMemberLike,Long> {

    @Query("select count(*) from TodoMemberLike tml where tml.todoMember.todoMemberId is :todoMemberId and tml.user.userId is :userId")
    int checkExistTodoLike(@Param("todoMemberId") Long todoMemberId,@Param("userId") Long userId);

    @Query("select count(*) from TodoMemberLike tml where tml.todoMember.goalMember.member.userId is :userId")
    int countTotalTodoLike(@Param("userId") Long userId);

    @Query("select count(*) from TodoMemberLike tml where tml.user.userId is :userId")
    int countPushTotalTodoLike(@Param("userId") Long userId);
}
