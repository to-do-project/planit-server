package com.planz.planit.src.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.planz.planit.config.BaseException;
import com.planz.planit.config.fcm.FirebaseCloudMessageService;
import com.planz.planit.src.domain.goal.*;
import com.planz.planit.src.domain.todo.*;
import com.planz.planit.src.domain.todo.dto.*;
import com.planz.planit.src.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.notification.NotificationSmallCategory.*;
import static java.time.LocalDateTime.now;

@Slf4j
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final TodoMemberRepository todoMemberRepository;
    private final TodoMemberLikeRepository todoMemberLikeRepository;
    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @Autowired
    public TodoService(TodoRepository todoRepository, GoalService goalService, UserService userService, TodoMemberRepository todoMemberRepository, TodoMemberLikeRepository todoMemberLikeRepository, NotificationService notificationService, DeviceTokenService deviceTokenService, FirebaseCloudMessageService firebaseCloudMessageService) {
        this.todoRepository = todoRepository;
        this.goalService = goalService;
        this.userService = userService;
        this.todoMemberRepository = todoMemberRepository;
        this.todoMemberLikeRepository = todoMemberLikeRepository;
        this.notificationService = notificationService;
        this.deviceTokenService = deviceTokenService;
        this.firebaseCloudMessageService = firebaseCloudMessageService;
    }

    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public CreateTodoResDTO createTodo(Long userId, CreateTodoReqDTO createTodoReqDTO) throws BaseException {
        //?????? ???????????? ??????????????? ????????? ??????
        goalService.checkManager(userId, createTodoReqDTO.getGoalId());

        //Goal ????????????
        Goal goal = goalService.getGoal(createTodoReqDTO.getGoalId());

        //goal ????????? active??? ????????? ?????? ??????
        if(goal.getGoalStatus()!= GoalStatus.ACTIVE){
            throw new BaseException(NOT_ACTIVE_GOAL);
        }
        //?????? ??????
        try{
            Todo todo = Todo.builder()
                    .goal(goal)
                    .title(createTodoReqDTO.getTitle())
                    .build();
            //to do ??????
            todoRepository.save(todo);

            //?????? ?????? ??????
            List<GoalMember> goalMembers = goalService.getGoalMembers(createTodoReqDTO.getGoalId());
            GoalMember first = goalMembers.stream().filter(m -> m.getMember().getUserId() == userId)
                    .findAny().orElseThrow(()-> new BaseException(INVALID_GOAL_USER));
            //?????? ??? todo ?????? ??????
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
        //????????? ?????? ?????? ???????????? ????????????
        if(todoMember.getGoalMember().getMember().getUserId() !=userId){
            log.error("?????? ????????? ?????? ???????????????.");
            throw new BaseException(NOT_EQUAL_TODO_USER);
        }
        //??? ??? ????????????
        if(!todoMember.checkTodo()){
            log.error("checkTodo(): error");
            throw new BaseException(FAILED_TO_CHECK_TODO);
        }
        try{
            todoMemberRepository.save(todoMember);

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CHECK_TODO);
        }
        //lastCheckAt ??????
        User user = userService.findUser(userId);
        addTmpExp(user,10);
        //??????????????? ??????
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

    public void sendCheckTodoMessage(Long userId, Long todoMemberId) throws BaseException {
        //?????? ?????? ????????????
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(NOT_EXIST_TODO_MEMBER));
        Goal goal = todoMember.getTodo().getGoal();
        //??????????????? return
        if(goal.getGroupFlag()!=GroupCategory.GROUP){
            return;
        }
        //?????? ?????? ????????????
        User user = userService.findUser(userId);
        //?????? ????????????
        List<GoalMember> goalMembers = goalService.getGoalMembers(goal.getGoalId());
        for (GoalMember goalMember : goalMembers) {
            if(goalMember.getMember().getUserId()==userId) continue;
            //?????? ????????? ??????
            notificationService.createNotification(goalMember.getMember(),GROUP_DONE,goal.getTitle()+" ?????? ????????? "+user.getNickname()+" ?????? "+
                todoMember.getTodo().getTitle()+" ??? ??????????????????.",null,null,null);
            try {
                //???????????? ??????
                List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_groupFlag1(goalMember.getMember());
                firebaseCloudMessageService.sendMessageTo(deviceTokenList, "[?????? Todo]", "??????????????? ?????? ??? ????????? to-do??? ??????????????????. ????????? to-do??? ?????? ??????????????????!");
            }catch (BaseException e){
                log.error("[FCM ?????? ??????] " + e.getStatus());
            }
        }
    }

    /**
     * ?????? ?????? ??????
     * @param userId
     * @param todoMemberId
     * @return
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public CheckTodoResDTO uncheckTodo(Long userId, Long todoMemberId) throws BaseException {
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(INVALID_TODO_MEMBER_ID));
        //????????? ?????? ?????? ???????????? ????????????
        if (todoMember.getGoalMember().getMember().getUserId() != userId) {
            log.error("?????? ????????? ?????? ???????????????.");
            throw new BaseException(NOT_EQUAL_TODO_USER);
        }
        //??? ??? ????????????
        if(!todoMember.uncheckTodo()){
            log.error("uncheckTodo(): error");
            throw new BaseException(FAILED_TO_UNCHECK_TODO);
        }
        try {
            todoMemberRepository.save(todoMember);

        } catch (Exception e) {
            throw new BaseException(FAILED_TO_UNCHECK_TODO);
        }
        //lastCheckAt ??????
        User user = userService.findUser(userId);
        addTmpExp(user, -10);
        //??????????????? ??????
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
        //?????? ???????????? ???????????? ???????????? ??????
        if(userId.equals(todoMember.getGoalMember().getMember().getUserId())){
            throw new BaseException(EQUAL_TODO_MEMBER_ID);
        }
        //?????? ????????? ?????????????????? ??????
        if(todoMember.getCompleteFlag()==CompleteFlag.INCOMPLETE){
            throw new BaseException(INVALID_COMPLETE_FLAG);
        }
        //?????? ???????????? ??????
        if(todoMemberLikeRepository.checkExistTodoLike(todoMemberId,userId)!=0){
            throw new BaseException(ALREADY_LIKE_TODO);
        }
        try{
            //????????? ????????????? ?????? ?????? ?????? ???????????? ????????????
            User user = userService.findUser(userId);
            //????????? ?????????
            TodoMemberLike todoMemberLike = TodoMemberLike.builder()
                    .todoMember(todoMember)
                    .user(user)
                    .build();
            todoMemberLikeRepository.save(todoMemberLike);
            addTmpExp(user,5); //????????? ?????? ??????
        }catch(Exception e){
            throw new BaseException(FAILED_TO_LIKE_TODO);
        }
        //?????????
        User member = todoMember.getGoalMember().getMember();
        addTmpExp(member,1);

        //fcm ?????????
        sendLikeTodoMessage(userId,todoMemberId);
    }
    //????????? ?????? ?????????
    public void sendLikeTodoMessage(Long userId, Long todoMemberId) throws BaseException {
        //????????? id ????????????
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(NOT_EXIST_TODO_MEMBER));
        User toUser = todoMember.getGoalMember().getMember();

        //??? id ????????????
        User user = userService.findUser(userId);

        notificationService.createNotification(toUser,TODO_FAVORITE,user.getNickname()+" ?????? "+todoMember.getTodo().getTitle()+" ??? ???????????? ???????????????."
        ,null,null,null);
        try{
            List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_friendFlag1(toUser.getUserId());
            firebaseCloudMessageService.sendMessageTo(deviceTokenList,"[?????????]","?????? ??? ????????? ???????????? ???????????????. ????????? ??????????????????!");
        }catch (BaseException e){
            log.error("[FCM ?????? ??????] " + e.getStatus());
        }
    }

    //????????? ????????? ??????
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
        //?????? ????????? ??????
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

    /**
     * ?????? ???????????? API
     * @param userId
     * @param todoMemberId
     */
    public void cheerUpTodo(Long userId, Long todoMemberId) throws BaseException {
        //???????????? ??????
        TodoMember todoMember = todoMemberRepository.findById(todoMemberId).orElseThrow(() -> new BaseException(NOT_EXIST_TODO_MEMBER));
        if(todoMember.getCompleteFlag()==CompleteFlag.COMPLETE){
            throw new BaseException(ALREADY_COMPLETE_TODO);
        }

        //???????????? ?????? ?????????
        User member = todoMember.getGoalMember().getMember();
        User user = userService.findUser(userId);
        notificationService.createNotification(member,TODO_CHEER,user.getNickname()+" ?????? "+todoMember.getTodo().getTitle()+"??? ??????????????????."
        ,null,null,null);
        try {
            List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_friendFlag1(member.getUserId());
            firebaseCloudMessageService.sendMessageTo(deviceTokenList,"[??????]","?????? ??? ????????? ????????? ???????????????. ?????? ????????? ??????????????????!");
        }catch (BaseException e){
            log.error("[FCM ?????? ??????] " + e.getStatus());
        }
    }
}
