package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.deviceToken.dto.ChangeFlagReqDTO;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.service.DeviceTokenService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.planz.planit.config.BaseResponseStatus.SUCCESS;

@RestController
@RequestMapping("/api")
public class DeviceTokenController {
    private final DeviceTokenService deviceTokenService;

    @Autowired
    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/device-token/{userId}")
    @ApiOperation(value="유저 디바이스 토큰 생성")
    public BaseResponse createDeviceToken(@PathVariable Long userId, @RequestBody DeviceTokenReqDTO reqDTO){
        try{
            deviceTokenService.createDeviceToken(userId,reqDTO);
            return new BaseResponse(SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    /**
     *유저 디바이스 토큰 갱신 구현 필요!!!!!
     */

    @PatchMapping("/device-token/{userId}")
    @ApiOperation(value="알림 설정 on off")
    public BaseResponse changeFlag(@PathVariable Long userId, @Valid @RequestBody ChangeFlagReqDTO reqDTO, BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        try{
            deviceTokenService.changeFlag(userId,reqDTO);
            return new BaseResponse(SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

}

