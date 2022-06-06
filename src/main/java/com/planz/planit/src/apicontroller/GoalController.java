package com.planz.planit.src.apicontroller;

import com.google.api.Http;
import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.goal.dto.CreateGoalReqDTO;
import com.planz.planit.src.domain.goal.dto.GetGoalDetailResDTO;
import com.planz.planit.src.domain.goal.dto.ModifyGoalReqDTO;
import com.planz.planit.src.domain.user.dto.GoalSearchUserResDTO;
import com.planz.planit.src.service.GoalService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@RestController
@RequestMapping("/api")
public class GoalController {
    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }


    @PostMapping("/goals")
    @ApiOperation("목표 추가 api")
    public BaseResponse createGoal(HttpServletRequest request, @Valid @RequestBody CreateGoalReqDTO createGoalReqDTO, BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            goalService.createGoal(userId, createGoalReqDTO);
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

        /*
        위에 return 코드 지우고 FCM 알림 전송 필요
        FCM 전송 후에 return 해야한다.
         */
    }

    @DeleteMapping("/goals")
    @ApiOperation("목표삭제/탈퇴 api")
    public BaseResponse deleteGoal(HttpServletRequest request, @RequestParam("goal") Long goalId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            goalService.deleteGoal(userId,goalId);
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PatchMapping("/goals")
    @ApiOperation("목표 수정 api -매니저만 가능")
    public BaseResponse modifyGoal(HttpServletRequest request, @Valid @RequestBody ModifyGoalReqDTO modifyGoalreqDTO,BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            goalService.modifyGoal(userId,modifyGoalreqDTO);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    @PatchMapping("/goals/{goalId}")
    @ApiOperation("그룹 초대 수락 api")
    public BaseResponse acceptGroupGoal(HttpServletRequest request,@PathVariable("goalId") Long goalId, @RequestParam("accept") int accept){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            goalService.acceptGroupGoal(goalId,userId,accept);
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch (BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @GetMapping("/goals/users")
    @ApiOperation("그룹 목표 닉네임 검색 api")
    public BaseResponse<GoalSearchUserResDTO> goalSearchUser(HttpServletRequest request,@RequestParam("nickname") String nickname){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            GoalSearchUserResDTO resDTO = goalService.goalSearchUser(userId,nickname);
            return new BaseResponse<>(resDTO);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/goals/{goalId}")
    @ApiOperation("목표 상세 조회 api")
    public BaseResponse<GetGoalDetailResDTO> getGoalDetail(HttpServletRequest request,@PathVariable("goalId") Long goalId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            GetGoalDetailResDTO resDTO = goalService.getGoalDetail(userId,goalId);
            return new BaseResponse<>(resDTO);

        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}
