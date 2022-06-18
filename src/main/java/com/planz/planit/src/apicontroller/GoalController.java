package com.planz.planit.src.apicontroller;

import com.google.api.Http;
import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.goal.dto.*;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.dto.GoalSearchUserResDTO;
import com.planz.planit.src.service.GoalService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.NOT_FRIEND_RELATION;
import static com.planz.planit.config.BaseResponseStatus.SUCCESS;


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
    public BaseResponse<String> createGoal(HttpServletRequest request, @Valid @RequestBody CreateGoalReqDTO createGoalReqDTO, BindingResult br) {
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            goalService.createGoal(userId, createGoalReqDTO);
            return new BaseResponse<>("목표 추가를 완료했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

        /*
        위에 return 코드 지우고 FCM 알림 전송 필요
        FCM 전송 후에 return 해야한다.
         */
    }

    @DeleteMapping("/goals")
    @ApiOperation("목표삭제/탈퇴 api")
    public BaseResponse<String> deleteGoal(HttpServletRequest request, @RequestParam("goal") Long goalId) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            goalService.deleteGoal(userId, goalId);
            return new BaseResponse<>("목표 삭제/탈퇴를 완료했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PatchMapping("/goals")
    @ApiOperation("목표 수정 api -매니저만 가능")
    public BaseResponse<String> modifyGoal(HttpServletRequest request, @Valid @RequestBody ModifyGoalReqDTO modifyGoalreqDTO, BindingResult br) {
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            goalService.modifyGoal(userId, modifyGoalreqDTO);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
        return new BaseResponse<>("목표 수정을 완료했습니다.");
    }

    @PatchMapping("/goals/{goalId}")
    @ApiOperation("그룹 초대 수락 api")
    public BaseResponse<String> acceptGroupGoal(HttpServletRequest request, @PathVariable("goalId") Long goalId, @RequestParam("accept") int accept) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            goalService.acceptGroupGoal(goalId, userId, accept);
            return new BaseResponse<>("그룹 초대 수락/거절을 완료했습니다.");
        } catch (BaseException e) {
            return new BaseResponse(e.getStatus());
        }
    }

    @GetMapping("/goals/users")
    @ApiOperation("그룹 목표 닉네임 검색 api")
    public BaseResponse<GoalSearchUserResDTO> goalSearchUser(HttpServletRequest request, @RequestParam("nickname") String nickname) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            GoalSearchUserResDTO resDTO = goalService.goalSearchUser(userId, nickname);
            return new BaseResponse<>(resDTO);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/goals/{goalId}")
    @ApiOperation("목표 상세 조회 api")
    public BaseResponse<GetGoalDetailResDTO> getGoalDetail(HttpServletRequest request, @PathVariable("goalId") Long goalId) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            GetGoalDetailResDTO resDTO = goalService.getGoalDetail(userId, goalId);
            return new BaseResponse<>(resDTO);

        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/goals/main/{targetUserId}")
    @ApiOperation("메인화면 조회 API - 투두")
    public BaseResponse<List<GetGoalMainInfoResDTO>> getGoalMainInfo(HttpServletRequest request, @PathVariable("targetUserId") Long targetUserId) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            if (userId != targetUserId) {
                return new BaseResponse<>(goalService.getFriendGoalMainInfo(userId, targetUserId));
            }
            return new BaseResponse<>(goalService.getGoalMainInfo(targetUserId)); //나 자신일 때
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PostMapping("/goals/archive/{goalId}")
    @ApiOperation("목표 보관함 API")
    public BaseResponse<String> changeToArchive(HttpServletRequest request, @PathVariable("goalId") Long goalId) {
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try {
            goalService.changeToArchive(userId, goalId);
            return new BaseResponse<>("목표 보관하기를 완료했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/goals/archive")
    @ApiOperation("목표 보관함 조회 API")
    public BaseResponse<List<GetArchiveGoalResDTO>> getArchiveGoals(HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            List<GetArchiveGoalResDTO> response = goalService.getArchiveGoals(userId);
            return new BaseResponse<>(response);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PatchMapping("/goals/archive/{goalId}")
    @ApiOperation("목표 보관함 활성화API")
    public BaseResponse<String> archiveToActive(HttpServletRequest request, @PathVariable Long goalId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            goalService.archiveToActive(goalId);
            return new BaseResponse<>("목표 시작하기를 완료했습니다.");
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}
