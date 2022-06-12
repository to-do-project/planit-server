package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.notification.dto.GetNotificationsResDTO;
import com.planz.planit.src.service.NotificationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 모든 알림을 조회한다.
     * - 공지사항 2개 최상단
     * - 확정하지 않은 친구 요청, 그룹 초대 요청은 최상단
     * - 나머지 알림은 시간순으로 최대 200개까지 조회 가능
     * @RequestHeader User-Id, Jwt-Access-Token
     * @return
     */
    @GetMapping("")
    @ApiOperation(value = "알림 조회 API")
    public BaseResponse<GetNotificationsResDTO> getNotifications(HttpServletRequest request){

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));

        try{
            return new BaseResponse(notificationService.getNotifications(userId));
        }
        catch (BaseException e){
            return new BaseResponse(e.getStatus());
        }
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
