package com.planz.planit.src.domain.deviceToken;

import com.planz.planit.src.domain.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken,Long> {

    //특정 디바이스 토큰 객체 가져오기 (userId+deviceToken)
    @Query("select d from DeviceToken d where d.user.userId is :userId and d.deviceToken is :deviceToken")
    public DeviceToken findDeviceTokenByUserIdAndDeviceToken(@Param("userId")Long userId,@Param("deviceToken") String deviceToken);
    //기존 디바이스 토큰이 존재하는지 확인
    public boolean existsByUserAndDeviceToken(User user, String deviceToken);

    // 해당 userId가 가진 모든 디바이스 토큰 삭제하기 => 혜지 추가
    @Transactional
    @Modifying
    @Query("delete from DeviceToken d where d.user.userId = :userId")
    void deleteByUserIdInQuery(@Param("userId") Long userId);

    // 해당 userId가 가진 모든 디바이스 토큰 검색
    @Transactional
    @Modifying
    @Query("select d.deviceTokenId from DeviceToken d where d.user.userId = :userId")
    List<Long> findAllByUserIdInQuery(@Param("userId") Long userId);

    // 가출 프로시저 호출
    @Query(value = "CALL run_away();", nativeQuery = true)
    List<DeviceToken> callRunAwayProcedure();

    // 운영자 매일 미션 프로시저 호출
    @Query(value = "CALL mission();", nativeQuery = true)
    List<DeviceToken> callMissionProcedure();

    @Query(value = "select d.deviceToken from DeviceToken d where d.noticeFlag = 1")
    List<String> findAllDeviceTokens_noticeFlag1();

    @Query(value = "select d.deviceToken from DeviceToken d where d.user.userId = :userId and d.friendFlag = 1")
    List<String> findAllDeviceTokens_friendFlag1(@Param("userId") Long userId);

    @Query(value="select d.deviceToken from DeviceToken d where d.user = :user and d.groupFlag=1")
    List<String> findAllDeviceTokens_groupFlag1(@Param("user") User user);
}
