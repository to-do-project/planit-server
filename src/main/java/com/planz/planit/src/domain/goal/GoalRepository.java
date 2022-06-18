package com.planz.planit.src.domain.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GoalRepository extends JpaRepository<Goal,Long> {

    @Transactional
    @Modifying
    @Query("update Goal g set g.goalStatus='DELETE' where g.goalId is :goalId")
    void deleteGoalById(@Param("goalId") Long goalId);
}
