package com.planz.planit.src.domain.deviceToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken,Long> {

    //디바이스 토큰 객체 가져오기
    public DeviceToken findDeviceTokenByUserIdAndDeviceToken(Long userId,String deviceToken);
    //기존 디바이스 토큰이 존재하는지 확인
    public boolean existsByUserIdAndDeviceToken(Long userId,String deviceToken);
}
