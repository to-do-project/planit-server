package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.todo.dto.CheckTodoResDTO;
import com.planz.planit.src.domain.todo.dto.CreateTodoReqDTO;
import com.planz.planit.src.service.GoalService;
import com.planz.planit.src.service.TodoService;
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
    public BaseResponse createTodo(HttpServletRequest request, @Valid @RequestBody CreateTodoReqDTO createTodoReqDTO, BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();

        try {
            todoService.createTodo(userId,createTodoReqDTO);
            return new BaseResponse(SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @PostMapping("/todo/{todoMemberId}")
    public BaseResponse<CheckTodoResDTO> checkTodo(HttpServletRequest request, @PathVariable(name="todoMemberId") Long todoMemberId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            todoService.checkTodo(userId,todoMemberId);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
        return new BaseResponse(SUCCESS);
    }

    @PostMapping("/todo/like/{goalId}")
    public BaseResponse likeTodo(HttpServletRequest request,@RequestParam(name="todoMemberId") Long todoMemberId){
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME)).longValue();
        try{
            todoService.likeTodo(userId,todoMemberId);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
        //FCM 날리기
        return new BaseResponse(SUCCESS);
    }
}
