package com.planz.planit.src.domain.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoMemberLikeRepository extends JpaRepository<TodoMemberLike,Long> {

    @Query("select count(*) from TodoMemberLike tml where tml.todoMember.todoMemberId is :todoMemberId and tml.user.userId is :userId")
    int checkExistTodoLike(@Param("todoMemberId") Long todoMemberId,@Param("userId") Long userId);
}
