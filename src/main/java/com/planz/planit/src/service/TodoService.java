package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.goal.Goal;
import com.planz.planit.src.domain.goal.GoalMember;
import com.planz.planit.src.domain.todo.*;
import com.planz.planit.src.domain.todo.dto.CreateTodoReqDTO;
import com.planz.planit.src.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.*;

@Slf4j
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final TodoMemberRepository todoMemberRepository;
    private final TodoMemberLikeRepository todoMemberLikeRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository, GoalService goalService, UserService userService, TodoMemberRepository todoMemberRepository, TodoMemberLikeRepository todoMemberLikeRepository) {
        this.todoRepository = todoRepository;
        this.goalService = goalService;
        this.userService = userService;
        this.todoMemberRepository = todoMemberRepository;
        this.todoMemberLikeRepository = todoMemberLikeRepository;
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void createTodo(Long userId, CreateTodoReqDTO createTodoReqDTO) throws BaseException {
        //투두 생성자가 매니저인지 아닌지 확인
        goalService.checkManager(userId, createTodoReqDTO.getGoalId());

        //Goal 가져오기
        Goal goal = goalService.getGoal(createTodoReqDTO.getGoalId());

        //goal 상태가 active가 아니면 에러 처리

        //투두 생성
        try{
            Todo todo = Todo.builder()
                    .goal(goal)
                    .title(createTodoReqDTO.getTitle())
                    .build();
            //to do 생성
            todoRepository.save(todo);

            //목표 멤버 조회
            List<GoalMember> goalMembers = goalService.getGoalMembers(createTodoReqDTO.getGoalId());

            //멤버 당 todo 상태 저장
            for(GoalMember goalMember:goalMembers){
                TodoMember todoMember = TodoMember.builder()
                        .todo(todo)
                        .goalMember(goalMember)
                        .build();
                todoMemberRepository.save(todoMember);
            }

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CREATE_TODO);
        }
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void checkTodo(Long userId, Long todoMemberId) throws BaseException {
        try{
            //유저가 투두 그룹 멤버인지 확인하기
            TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(INVALID_TODO_MEMBER_ID));
            if(todoMember.getGoalMember().getMember().getUserId() !=userId){
                log.error("투두 멤버와 다른 유저입니다.");
                throw new BaseException(NOT_EQUAL_TODO_USER);
            }
            //할 일 체크하기
            todoMember.checkTodo();
            todoMemberRepository.save(todoMember);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_CHECK_TODO);
        }
    }


    public void likeTodo(Long userId, Long todoMemberId) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //자기 자신한테 좋아요를 누르는지 확인
        if(userId.equals(todoMember.getGoalMember().getMember().getUserId())){
            throw new BaseException(EQUAL_TODO_MEMBER_ID);
        }
        //상대 목표가 완료상태인지 확인
        if(todoMember.getCompleteFlag()==CompleteFlag.INCOMPLETE){
            throw new BaseException(INVALID_COMPLETE_FLAG);
        }
        //이미 눌렀는지 확인
        if(todoMemberLikeRepository.checkExistTodoLike(todoMemberId,userId)!=0){
            throw new BaseException(ALREADY_LIKE_TODO);
        }
        try{
            //목표가 비공개면? 같은 그룹 내의 사람인지 확인하기
            User user = userService.findUser(userId);
            //좋아요 누르기
            TodoMemberLike todoMemberLike = TodoMemberLike.builder()
                    .todoMember(todoMember)
                    .user(user)
                    .build();
            todoMemberLikeRepository.save(todoMemberLike);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_LIKE_TODO);
        }
    }
}
