package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.friend.dto.AcceptReqDTO;
import com.planz.planit.src.domain.friend.dto.FollowReqDTO;
import com.planz.planit.src.domain.friend.dto.GetFriendResDTO;
import com.planz.planit.src.service.FriendService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.SERVER_ERROR;
import static com.planz.planit.config.BaseResponseStatus.SUCCESS;

@RestController
@RequestMapping("/api")
public class FriendController {
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
    @PostMapping("/friend/{toUserId}")
    @ApiOperation(value = "친구 요청 api")
    public BaseResponse<Friend> followUser(@PathVariable Long toUserId, @RequestBody FollowReqDTO followReqDTO) throws BaseException {
        /*
        Authnetificattion 과정
         */
        try{
            friendService.save(followReqDTO.getUserId(),toUserId);
            return new BaseResponse<>(SUCCESS);
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
    @GetMapping("/friend/{userId}")
    @ApiOperation("친구 목록 조회 api")
    public BaseResponse<List<GetFriendResDTO>> getFriends(@PathVariable Long userId) throws BaseException{
        try {
            List<GetFriendResDTO> friends = friendService.getFriends(userId);
            return new BaseResponse<>(friends);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 수락 - true
     * 거절 - false
     */
    @PatchMapping("/friend/{userId}")
    @ApiOperation("친구 요청 수락/거절/삭제 api")
    public BaseResponse acceptFriends(@PathVariable Long userId, @RequestBody AcceptReqDTO acceptReqDTO){
       try {
           if (friendService.acceptFriend(acceptReqDTO)) {
               return new BaseResponse<>(SUCCESS);
           }
           return new BaseResponse(SERVER_ERROR); //에러
       }catch(BaseException e){
           return new BaseResponse(e.getStatus());
       }
    }


}
