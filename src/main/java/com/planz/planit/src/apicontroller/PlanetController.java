package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.planet.dto.GetPlanetMainInfoResDTO;
import com.planz.planit.src.domain.planet.dto.GetPlanetMyInfoResDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.service.FriendService;
import com.planz.planit.src.service.PlanetService;
import com.planz.planit.src.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.planz.planit.config.BaseResponseStatus.NOT_FRIEND_RELATION;

@RequestMapping("/api/planet")
@RestController
public class PlanetController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final PlanetService planetService;
    private final FriendService friendService;
    private final UserService userService;

    @Autowired
    public PlanetController(PlanetService planetService, FriendService friendService, UserService userService) {
        this.planetService = planetService;
        this.friendService = friendService;
        this.userService = userService;
    }

    /**
     * 타겟 유저의 행성 관련 정보를 모두 반환한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @PathVariable targetUserId
     * @return 행성 레벨, 현재 사용중인 캐릭터 아이템 아이디, 현재 사용중인 행성 아이템 아이디 + 위치
     */
    @GetMapping("/main/{targetUserId}")
    @ApiOperation(value = "행성 메인 화면 조회 API")
    public BaseResponse<GetPlanetMainInfoResDTO> getPlanetMainInfo(HttpServletRequest request, @PathVariable Long targetUserId){

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));

        try{
            // 친구의 행성 정보를 조회하는 경우 => 나와 친구 관계인지 validation
            if (userId != targetUserId) {

                User myUser = userService.findUser(userId);
                User friendUser = userService.findUser(targetUserId);
                if (friendService.isFriend(myUser, friendUser) == false) {
                    return new BaseResponse<>(NOT_FRIEND_RELATION);
                }
            }

            return new BaseResponse<>(planetService.getPlanetMainInfo(targetUserId));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 나의 행성 정보를 조회한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @return 행성 나이, 행성 레벨, 보유 포인트, 평균 목표 완성률, 좋아요 받은 수, 좋아요 누른 수
     */
    @GetMapping("/my-info")
    @ApiOperation("나의 행성 정보 조회 API")
    public BaseResponse<GetPlanetMyInfoResDTO> getPlanetMyInfo(HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));

        try{
            return new BaseResponse<>(planetService.getPlanetMyInfo(userId));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
