package com.planz.planit.src.domain.deviceToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken,Long> {

    //디바이스 토큰 객체 가져오기
    public DeviceToken findDeviceTokenByUserAndDeviceToken(Long userId,String deviceToken);
    //기존 디바이스 토큰이 존재하는지 확인
    public boolean existsByUserAndDeviceToken(Long userId,String deviceToken);
}
