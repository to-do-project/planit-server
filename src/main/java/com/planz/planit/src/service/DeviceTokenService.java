package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.DeviceTokenFlag;
import com.planz.planit.src.domain.deviceToken.DeviceTokenRepository;
import com.planz.planit.src.domain.deviceToken.dto.ChangeFlagReqDTO;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserService userService;

    @Autowired
    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository, UserService userService) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.userService = userService;
    }

    public void createDeviceToken(Long userId, DeviceTokenReqDTO reqDTO) throws BaseException {
        //이미 존재하는지 확인
        try {
            if (deviceTokenRepository.existsByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken())) {
                DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
                findToken.changeUpdateAt();
                deviceTokenRepository.save(findToken);
            }
            else{ //존재하지 않는 경우에는 생성해준다.
                DeviceToken deviceToken = DeviceToken.builder()
                        .user(userService.findUser(userId))
                        .deviceToken(reqDTO.getDeviceToken())
                        .build();
                deviceTokenRepository.save(deviceToken);
            }
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //로그아웃 시 디바이스 토큰 삭제 - DTO 수정 필요
    public void deleteDeviceToken(Long userId, DeviceTokenReqDTO reqDTO) throws BaseException {
        try{
            DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
            deviceTokenRepository.delete(findToken);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void changeFlag(Long userId, ChangeFlagReqDTO reqDTO) throws BaseException {
        try{
            DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
            findToken.changeFlag(DeviceTokenFlag.valueOf(reqDTO.getFlag()));
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
