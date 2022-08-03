package com.planz.planit.src.domain.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GoalMemberRepository extends JpaRepository<GoalMember,Long> {

    @Query("select gm from GoalMember gm where gm.status <> 'REJECT' and  gm.goal.goalStatus <> 'DELETE' and gm.goal.goalId is :goalId ")
    List<GoalMember> findGoalMembersByGoal(@Param("goalId") Long goalId);

    @Query("select gm from GoalMember gm where gm.status <> 'REJECT' and gm.goal.goalId is :goalId and gm.member.userId is :userId")
    Optional<GoalMember> findGoalMemberByGoalAndUser(@Param("goalId") Long goalId, @Param("userId") Long userId);

    @Query("select gm.memberRole from GoalMember gm where gm.member.userId is :userId and gm.goal.goalId is :goalId")
    GoalMemberRole checkMemberRole(@Param("userId")Long userId, @Param("goalId")Long goalId);

    //그룹 내 멤버들 모두 삭제
    @Transactional
    @Modifying
    @Query("update GoalMember gm set gm.status='REJECT' where gm.goal.goalId is :goalId")
    void deleteAllGoalMemberInQuery(@Param("goalId") Long goalId);

    //특정 멤버만 삭제
    @Transactional
    @Modifying
    @Query("update GoalMember gm set gm.status='REJECT' where gm.member.userId is :userId and gm.goal.goalId is :goalId")
    void deleteGoalMemberInQuery(@Param("userId") Long userId, @Param("goalId") Long goalId);

    @Query("select count(gm.goalMemberId) from GoalMember gm where gm.goal.goalId is :goalId and gm.member.userId is :userId")
    int checkGoalMember(@Param("goalId") Long goalId, @Param("userId") Long userId);

    @Query("select gm from GoalMember gm where gm.member.userId is :userId")
    List<GoalMember> findGoalMembersByMember(@Param("userId") Long userId);

    @Query("select gm.goal from GoalMember gm where gm.goal.goalStatus= 'ARCHIVE' and gm.memberRole='MANAGER' and gm.member.userId is :userId")
    List<Goal> getGoalArchivesByMember(@Param("userId") Long userId);

    @Query("select gm.goal from GoalMember gm where gm.member.userId = :userId and gm.memberRole = :role")
    List<Goal> getGoalsByUserIdAndRole(@Param("userId") Long userId, @Param("role") GoalMemberRole role);

    @Query("select gm.goal from GoalMember gm where gm.member.userId= :userId and gm.goal.groupFlag='MISSION'")
    Goal getGoalByMissionAndUser(@Param("userId") Long userId);

}
