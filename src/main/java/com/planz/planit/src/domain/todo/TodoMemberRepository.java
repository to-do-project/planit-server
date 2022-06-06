package com.planz.planit.src.domain.todo;

import com.planz.planit.src.domain.goal.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoMemberRepository extends JpaRepository<TodoMember,Long>{

    @Query("select tm from TodoMember tm where tm.todo.todoId is :todoId and tm.goalMember.goalMemberId is :goalMemberId")
    TodoMember findTodoMemberByTodoAndGoalMember(@Param("todoId") Long todoId, @Param("goalMemberId") Long goalMemberId);
}
