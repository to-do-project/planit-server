package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.service.NotificationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 알림의 readStatus를 읽음 상태 (READ)로 변경한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @PathVariable notificationId
     * @return 결과 메세지
     */
    @PatchMapping("/read/{notificationId}")
    @ApiOperation(value = "알림 읽음 처리 API")
    public BaseResponse<String> readNotification(HttpServletRequest request,
                                                 @PathVariable Long notificationId){

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));

        try {
            notificationService.readNotification(notificationId, userId);
            return new BaseResponse<>("성공적으로 해당 알림에 대한 읽음 처리를 진행했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
