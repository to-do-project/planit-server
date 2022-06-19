package com.planz.planit.src.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.goal.*;
import com.planz.planit.src.domain.todo.*;
import com.planz.planit.src.domain.todo.dto.*;
import com.planz.planit.src.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static java.time.LocalDateTime.now;

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
    public CreateTodoResDTO createTodo(Long userId, CreateTodoReqDTO createTodoReqDTO) throws BaseException {
        //투두 생성자가 매니저인지 아닌지 확인
        goalService.checkManager(userId, createTodoReqDTO.getGoalId());

        //Goal 가져오기
        Goal goal = goalService.getGoal(createTodoReqDTO.getGoalId());

        //goal 상태가 active가 아니면 에러 처리
        if(goal.getGoalStatus()!= GoalStatus.ACTIVE){
            throw new BaseException(NOT_ACTIVE_GOAL);
        }
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
            GoalMember first = goalMembers.stream().filter(m -> m.getMember().getUserId() == userId)
                    .findAny().orElseThrow(()-> new BaseException(INVALID_GOAL_USER));
            //멤버 당 todo 상태 저장
            for(GoalMember goalMember:goalMembers){
                TodoMember todoMember = TodoMember.builder()
                        .todo(todo)
                        .goalMember(goalMember)
                        .build();
                todoMemberRepository.save(todoMember);
            }

            TodoMember result = todoMemberRepository.findTodoMemberByTodoAndGoalMember(todo.getTodoId(), first.getGoalMemberId());
            return new CreateTodoResDTO(result.getTodoMemberId());

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CREATE_TODO);
        }
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public CheckTodoResDTO checkTodo(Long userId, Long todoMemberId) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(INVALID_TODO_MEMBER_ID));
        //유저가 투두 그룹 멤버인지 확인하기
        if(todoMember.getGoalMember().getMember().getUserId() !=userId){
            log.error("투두 멤버와 다른 유저입니다.");
            throw new BaseException(NOT_EQUAL_TODO_USER);
        }
        //할 일 체크하기
        if(!todoMember.checkTodo()){
            log.error("checkTodo(): error");
            throw new BaseException(FAILED_TO_CHECK_TODO);
        }
        try{
            todoMemberRepository.save(todoMember);

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CHECK_TODO);
        }
        //lastCheckAt 정리
        User user = userService.findUser(userId);
        addTmpExp(user,10);
        //퍼센테이지 리턴
        try {
            Long goalMemberId = todoMember.getGoalMember().getGoalMemberId();
            List<TodoMember> todoMembersByGoalMemberId = todoMemberRepository.findTodoMembersByGoalMemberId(goalMemberId);
            int completeCount = todoMembersByGoalMemberId.stream().filter(m -> m.getCompleteFlag() == CompleteFlag.COMPLETE).collect(Collectors.toList()).size();
            int percentage = todoMembersByGoalMemberId.size()==0?0:100*completeCount/todoMembersByGoalMemberId.size();
            return new CheckTodoResDTO(percentage);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_GET_PERCENTAGE);
        }
    }

    /**
     * 투두 체크 취소
     * @param userId
     * @param todoMemberId
     * @return
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public CheckTodoResDTO uncheckTodo(Long userId, Long todoMemberId) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(INVALID_TODO_MEMBER_ID));
        //유저가 투두 그룹 멤버인지 확인하기
        if (todoMember.getGoalMember().getMember().getUserId() != userId) {
            log.error("투두 멤버와 다른 유저입니다.");
            throw new BaseException(NOT_EQUAL_TODO_USER);
        }
        //할 일 체크해제
        if(!todoMember.uncheckTodo()){
            log.error("uncheckTodo(): error");
            throw new BaseException(FAILED_TO_UNCHECK_TODO);
        }
        try {
            todoMemberRepository.save(todoMember);

        } catch (Exception e) {
            throw new BaseException(FAILED_TO_UNCHECK_TODO);
        }
        //lastCheckAt 정리
        User user = userService.findUser(userId);
        addTmpExp(user, -10);
        //퍼센테이지 리턴
        try {
            Long goalMemberId = todoMember.getGoalMember().getGoalMemberId();
            List<TodoMember> todoMembersByGoalMemberId = todoMemberRepository.findTodoMembersByGoalMemberId(goalMemberId);
            int completeCount = todoMembersByGoalMemberId.stream().filter(m -> m.getCompleteFlag() == CompleteFlag.COMPLETE).collect(Collectors.toList()).size();
            int percentage = todoMembersByGoalMemberId.size() == 0 ? 0 : 100 * completeCount / todoMembersByGoalMemberId.size();
            return new CheckTodoResDTO(percentage);
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_GET_PERCENTAGE);
        }

    }
        private void addTmpExp(User user, int exp) throws BaseException {
        try {
            user.setLastCheckAt(now());
            user.addTmpPoint(exp);
            user.getPlanet().addTmpExp(exp);
            userService.saveUser(user);
        }catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
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
            addTmpExp(user,5); //좋아요 누른 사람
        }catch(Exception e){
            throw new BaseException(FAILED_TO_LIKE_TODO);
        }
        //상대방
        User member = todoMember.getGoalMember().getMember();
        addTmpExp(member,1);

    }

    //좋아요 리스트 조회
    public GetLikeTodoResDTO getLikeTodoMember(Long todoMemberId) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        if(todoMember.getCompleteFlag()==CompleteFlag.INCOMPLETE){
            throw new BaseException(INCOMPLETE_FLAG);
        }
        List<TodoMemberLike> todoMemberLikes = todoMember.getTodoMemberLikes();
        try {
            List<LikeUserResDTO> likeUserResList = new ArrayList<>();
            for (TodoMemberLike todoMemberLike : todoMemberLikes) {
                likeUserResList.add(new LikeUserResDTO(todoMemberLike.getUser().getUserId(), todoMemberLike.getUser().getNickname(),
                        todoMemberLike.getUser().getProfileColor().toString()));
            }
            return new GetLikeTodoResDTO(todoMemberLikes.size(), likeUserResList);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_GET_LIKES);
        }
    }

    public int getTotalTodoLike(Long userId) throws BaseException{
        try {
            return todoMemberLikeRepository.countTotalTodoLike(userId);
        }catch(Exception e){
            log.error("TodoService.getTotalTodoLike error");
            throw new BaseException(DATABASE_ERROR);
        }
    }
    public int getPushTotalTodoLike(Long userId) throws BaseException {
        try{
            return todoMemberLikeRepository.countPushTotalTodoLike(userId);
        }catch(Exception e){
            log.error("TodoService.getPushTotalTodoLike");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void changeTodoTitle(Long userId, Long todoMemberId, ChangeTodoReqDTO reqDTO) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId)
                .orElseThrow(() -> new BaseException(NOT_EXIST_TODO_MEMBER));
        //개인 목표가 아님
        if(todoMember.getGoalMember().getGoal().getGroupFlag()== GroupCategory.GROUP){
            throw new BaseException(NOT_GROUP_TODO);
        }
        try {
            Todo todo = todoMember.getTodo();
            todo.changeTitle(reqDTO.getTitle());
            todoRepository.save(todo);
        }catch (Exception e){
            throw new BaseException(FAILED_TO_CHANGE_TODO);
        }
    }


}
