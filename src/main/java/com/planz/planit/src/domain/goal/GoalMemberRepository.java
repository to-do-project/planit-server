package com.planz.planit.src.domain.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GoalMemberRepository extends JpaRepository<GoalMember,Long> {

    @Query("select gm from GoalMember gm where gm.goal.goalId is :goalId")
    List<GoalMember> findGoalMembersByGoal(@Param("goalId") Long goalId);

    @Query("select gm from GoalMember gm where gm.goal.goalId is :goalId and gm.member.userId is :userId")
    Optional<GoalMember> findGoalMemberByGoalAndUser(@Param("goalId") Long goalId, @Param("userId") Long userId);

    @Query("select gm.memberRole from GoalMember gm where gm.member.userId is :userId and gm.goal.goalId is :goalId")
    GoalMemberRole checkMemberRole(@Param("userId")Long userId, @Param("goalId")Long goalId);

    //그룹 내 멤버들 모두 삭제
    @Transactional
    @Modifying
    @Query("delete from GoalMember gm where gm.goal.goalId is:goalId")
    void deleteAllGoalMemberInQuery(@Param("goalId") Long goalId);

    //특정 멤버만 삭제
    @Transactional
    @Modifying
    @Query("delete from GoalMember gm where gm.member.userId is :userId and gm.goal.goalId is:goalId")
    void deleteGoalMemberInQuery(@Param("userId") Long userId, @Param("goalId") Long goalId);

    @Query("select count(gm.goalMemberId) from GoalMember gm where gm.goal.goalId is :goalId and gm.member.userId is :userId")
    int checkGoalMember(@Param("goalId") Long goalId, @Param("userId") Long userId);
}
