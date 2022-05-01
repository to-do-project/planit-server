package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.domain.deviceToken.DeviceTokenFlag;
import com.planz.planit.src.domain.deviceToken.DeviceTokenRepository;
import com.planz.planit.src.domain.deviceToken.dto.ChangeFlagReqDTO;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;
import static com.planz.planit.config.BaseResponseStatus.FAILED_TO_DELETE_DEVICE_TOKEN;

@Slf4j
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
            User user = userService.findUser(userId);
            if (deviceTokenRepository.existsByUserAndDeviceToken(user, reqDTO.getDeviceToken())) {
                log.info("DeviceTokenService.createDeviceToken() - 존재하는 경우 ");
                //존재하는 경우에는 updateAt
                DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
                findToken.changeUpdateAt();
                deviceTokenRepository.save(findToken);
            }
            else{ //존재하지 않는 경우에는 생성해준다.
                log.info("DeviceTokenService.createDeviceToken() - 존재하지 않는 경우");
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

    //로그아웃 시 디바이스 토큰 삭제
    public void deleteDeviceToken(Long userId, DeviceTokenReqDTO reqDTO) throws BaseException {
        try{
            DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
            deviceTokenRepository.delete(findToken);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_DELETE_DEVICE_TOKEN);
        }
    }

    //회원탈퇴 시 전체 디바이스 토큰 삭제
    public void deleteAllDeviceToken(Long userId) throws BaseException {
        try{
            deviceTokenRepository.deleteByUserIdInQuery(userId);
        }catch (Exception e){
            throw new BaseException(FAILED_TO_DELETE_DEVICE_TOKEN);
        }
    }

    //유저의 디바이스 토큰 id 조회
    public List<Long> findUserAllDeviceToken(Long userId) throws BaseException {
        try{
            return deviceTokenRepository.findAllByUserIdInQuery(userId);
        } catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }
    public void changeFlag(Long userId, ChangeFlagReqDTO reqDTO) throws BaseException {
        try{
            User user = userService.findUser(userId);
            DeviceToken findToken = deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, reqDTO.getDeviceToken());
            findToken.changeFlag(DeviceTokenFlag.valueOf(reqDTO.getFlag()));
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public DeviceToken findDeviceTokenByUserIdAndDeviceToken(Long userId, String deviceToken) throws BaseException {
        try{
            log.info("findDeviceTokenByUserIdAndDeviceToken() 호출");
            return deviceTokenRepository.findDeviceTokenByUserIdAndDeviceToken(userId, deviceToken);
        }
        catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
