package com.planz.planit.src.domain.todo;

import com.planz.planit.src.domain.goal.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo,Long> {

    @Query("select t.goal from Todo t where t.todoId is :todoId")
    Goal checkGoalByTodoId(@Param("todoId") Long todoId);
}
