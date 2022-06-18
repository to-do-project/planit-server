package com.planz.planit.src.apicontroller;

import com.google.api.Http;
import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.friend.dto.AcceptReqDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendListResDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendResDTO;
import com.planz.planit.src.service.FriendService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/api")
public class FriendController {
    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * fromUserId를 가진 user가 toUserId를 가진 user를 팔로우한다.
     * security 추가 - params의 userId 부분 수정 필요
     * @param toUserId
     * @Return 새로 생성된 follow 객체
     */
    @PostMapping("/friends/{toUserId}")
    @ApiOperation(value = "친구 요청 api")
    public BaseResponse<String> followUser(HttpServletRequest request, @PathVariable Long toUserId) throws BaseException {
        /*
        Authnetificattion 과정
         */
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        if(userId==toUserId){
            return new BaseResponse<>(EQUAL_TO_USER_ID);
        }
        try{
            friendService.save(userId,toUserId);
            return new BaseResponse<>("친구 요청을 완료했습니다.");
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
        /*
        FCM 보내기
         */
    }

    /**
     * 유저의 친구 목록 보여주기
     * jwt 이용하면 userId 사용안해도 됨
     * DTO로 변환해야 함
     * @return
     */
    @GetMapping("/friends")
    @ApiOperation("친구 목록 조회 api")
    public BaseResponse<GetFriendListResDTO> getFriends(HttpServletRequest request) throws BaseException{
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            GetFriendListResDTO friends = friendService.getFriends(userId);
            return new BaseResponse<>(friends);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 수락 - true
     * 거절 - false
     */
    @PatchMapping("/friends")
    @ApiOperation("친구 요청 수락/거절/삭제 api")
    public BaseResponse<String> acceptFriends(HttpServletRequest request, @RequestBody AcceptReqDTO acceptReqDTO){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
           if (friendService.acceptFriend(acceptReqDTO)) {
               return new BaseResponse<>("친구 요청 수락/거절을 완료했습니다.");
           }
           return new BaseResponse(SERVER_ERROR); //에러
       }catch(BaseException e){
           return new BaseResponse(e.getStatus());
       }
    }


}
