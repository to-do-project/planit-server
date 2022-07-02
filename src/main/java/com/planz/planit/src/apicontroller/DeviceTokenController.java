package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.deviceToken.dto.ChangeFlagReqDTO;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.deviceToken.dto.GetAlarmInfoResDTO;
import com.planz.planit.src.service.DeviceTokenService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.planz.planit.config.BaseResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class DeviceTokenController {
    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    @Value("${jwt.device-token-header-name}")
    private String DEVICE_TOKEN_HEADER_NAME;

    private final DeviceTokenService deviceTokenService;

    @Autowired
    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/device-token")
    @ApiOperation(value="유저 디바이스 토큰 생성")
    public BaseResponse<String> createDeviceToken(HttpServletRequest request, @RequestBody DeviceTokenReqDTO reqDTO){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            log.info("DeviceTokenController.createDeviceToken()");
            deviceTokenService.createDeviceToken(userId,reqDTO);
            return new BaseResponse<>("디바이스 토큰 생성을 완료했습니다.");
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @PatchMapping("/alarms")
    @ApiOperation(value="알림 설정 on off")
    public BaseResponse<String> changeFlag(HttpServletRequest request, @Valid @RequestBody ChangeFlagReqDTO reqDTO, BindingResult br){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        try{
            deviceTokenService.changeFlag(userId,reqDTO);
            return new BaseResponse<>(reqDTO.getFlag()+"알림 설정 변경을 완료했습니다.");
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @GetMapping("/alarms")
    @ApiOperation(value = "알림 설정 조회 API")
    public BaseResponse<GetAlarmInfoResDTO> getAlarmInfo(HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        String deviceToken = request.getHeader(DEVICE_TOKEN_HEADER_NAME);
        try{
            return new BaseResponse<>(deviceTokenService.getAlarmInfo(userId,deviceToken));
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}

