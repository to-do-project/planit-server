package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.todo.dto.*;
import com.planz.planit.src.service.GoalService;
import com.planz.planit.src.service.TodoService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.planz.planit.config.BaseResponseStatus.SUCCESS;

@RestController
@RequestMapping("/api")
public class TodoController {
    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final TodoService todoService;
    private final GoalService goalService;

    @Autowired
    public TodoController(TodoService todoService, GoalService goalService) {
        this.todoService = todoService;
        this.goalService = goalService;
    }

    @PostMapping("/todo")
    @ApiOperation("투두 추가 API")
    public BaseResponse<CreateTodoResDTO> createTodo(HttpServletRequest request, @Valid @RequestBody CreateTodoReqDTO createTodoReqDTO, BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();

        try {

            return new BaseResponse<>(todoService.createTodo(userId,createTodoReqDTO));
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @PatchMapping("/todo/{todoMemberId}")
    @ApiOperation("투두 체크 API")
    public BaseResponse<CheckTodoResDTO> checkTodo(HttpServletRequest request, @PathVariable(name="todoMemberId") Long todoMemberId, @RequestParam("flag") boolean flag){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            if(flag) {
                CheckTodoResDTO response = todoService.checkTodo(userId, todoMemberId);
                return new BaseResponse<>(response);
            }else{
                CheckTodoResDTO response = todoService.uncheckTodo(userId,todoMemberId);
                return new BaseResponse<>(response);
            }
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
//    @PatchMapping("/todo/{todoMemberId}")
//    @ApiOperation("투두 체크 해제 API")
//    public BaseResponse<CheckTodoResDTO> uncheckTodo(HttpServletRequest request, @PathVariable(name="todoMemberId") Long todoMemberId){
//        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
//        try{
//            CheckTodoResDTO response = todoService.uncheckTodo(userId,todoMemberId);
//            return new BaseResponse<>(response);
//        }catch(BaseException e){
//            return new BaseResponse<>(e.getStatus());
//        }
//    }

    @PostMapping("/todo/like/{todoMemberId}")
    @ApiOperation("투두 좋아요 API")
    public BaseResponse<String> likeTodo(HttpServletRequest request,@PathVariable(name="todoMemberId") Long todoMemberId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            todoService.likeTodo(userId,todoMemberId);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
        //FCM 날리기
        return new BaseResponse<>("투두 좋아요를 완료했습니다.");
    }

    @GetMapping("/todo/like/{todoMemberId}")
    @ApiOperation("투두 좋아요 리스트 조회 API")
    public BaseResponse<GetLikeTodoResDTO> getLikeTodoMember(HttpServletRequest request,@PathVariable(name="todoMemberId") Long todoMemberId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            GetLikeTodoResDTO response = todoService.getLikeTodoMember(todoMemberId);
            return new BaseResponse<>(response);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PostMapping("/todo/change/{todoMemberId}")
    @ApiOperation("투두 수정 API")
    public BaseResponse<String> changeTodoTitle(HttpServletRequest request,@PathVariable Long todoMemberId,@RequestBody ChangeTodoReqDTO reqDTO){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            todoService.changeTodoTitle(userId,todoMemberId,reqDTO);
            return new BaseResponse<>("투두 수정을 완료했습니다.");
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }
}
