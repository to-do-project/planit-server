package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.fcm.FirebaseCloudMessageService;
import com.planz.planit.src.domain.goal.*;
import com.planz.planit.src.domain.goal.dto.*;
import com.planz.planit.src.domain.todo.CompleteFlag;
import com.planz.planit.src.domain.todo.TodoMember;
import com.planz.planit.src.domain.todo.TodoMemberLike;
import com.planz.planit.src.domain.todo.dto.GetTodoMemberDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.dto.GoalSearchUserResDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.goal.GoalMemberRole.MANAGER;
import static com.planz.planit.src.domain.goal.GoalMemberRole.MEMBER;
import static com.planz.planit.src.domain.goal.GoalStatus.ACTIVE;
import static com.planz.planit.src.domain.goal.GoalStatus.ARCHIVE;
import static com.planz.planit.src.domain.goal.GroupStatus.ACCEPT;
import static com.planz.planit.src.domain.goal.GroupStatus.WAIT;
import static com.planz.planit.src.domain.notification.NotificationSmallCategory.GROUP_REQUEST;

@Slf4j
@Service
public class GoalService {
    private final UserService userService;
    private final FriendService friendService;
    private final NotificationService notificationService;
    private final GoalRepository goalRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final DeviceTokenService deviceTokenService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @Autowired
    public GoalService(UserService userService, FriendService friendService, NotificationService notificationService, GoalRepository goalRepository, GoalMemberRepository goalMemberRepository, DeviceTokenService deviceTokenService, FirebaseCloudMessageService firebaseCloudMessageService) {
        this.userService = userService;
        this.friendService = friendService;
        this.notificationService = notificationService;
        this.goalRepository = goalRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.deviceTokenService = deviceTokenService;
        this.firebaseCloudMessageService = firebaseCloudMessageService;
    }

    /**
     * ?????? ??????
     * @param userId
     * @param createGoalReqDTO
     * @return
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public boolean createGoal(Long userId, CreateGoalReqDTO createGoalReqDTO) throws BaseException {

        //????????? ?????? ????????????
        User manager = userService.findUser(userId);
        try{
            Goal goal = Goal.builder()
                    .title(createGoalReqDTO.getTitle())
                    .groupFlag(GroupCategory.valueOf(createGoalReqDTO.getGroupFlag()))
                    .openFlag(OpenCategory.valueOf(createGoalReqDTO.getOpenFlag()))
                    .build();

            //?????? ??????
            goalRepository.save(goal);
            //?????? ????????? - goalMember??? ????????? ??????
            GoalMember goalManager = GoalMember.builder()
                    .goal(goal)
                    .member(manager)
                    .status(ACCEPT) //???????????? ?????? ????????? accept
                    .memberRole(GoalMemberRole.MANAGER)
                    .build();

            goalMemberRepository.save(goalManager);

            //???????????? ?????? ?????? ??????
            if(createGoalReqDTO.getGroupFlag().equals("GROUP")){
                List<Long> memberIdList = createGoalReqDTO.getMemberList();
                for(Long memberId: memberIdList){
                    User user = userService.findUser(memberId);
                    GoalMember goalMember = GoalMember.builder()
                            .goal(goal)
                            .member(user)
                            .status(WAIT)
                            .memberRole(MEMBER)
                            .build();
                    goalMemberRepository.save(goalMember);

                    //notification ????????? ??????
                    notificationService.createNotification(user,GROUP_REQUEST,
                            createGoalReqDTO.getTitle()+" ??????????????? ?????????????????????.",null,goal,null);
                    //fcm?????? ??????
                    try{
                        List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_groupFlag1(goalMember.getMember());
                        firebaseCloudMessageService.sendMessageTo(deviceTokenList,"?????? ?????? ??????"
                                ,"????????? ??????????????? ?????????????????????. ?????? ????????? ??????????????????!"
                        );
                    }catch(BaseException e){
                        log.error("[FCM ?????? ??????] " + e.getStatus());
                    }
                }
            }

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CREATE_GOAL);
        }
        return true; //?????? ?????? ?????? ??? true
    }


    /**
     * ?????? ?????? / ??????
     * @param userId
     * @param goalId
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void deleteGoal(Long userId, Long goalId) throws BaseException {
        //?????? ????????? ???????????? ??????
        try{
            //??????????????? ??????
            if(goalMemberRepository.checkMemberRole(userId,goalId)==GoalMemberRole.MANAGER){
                goalRepository.deleteGoalById(goalId); //?????? ??????
                //?????? ?????? ???????
                goalMemberRepository.deleteAllGoalMemberInQuery(goalId);

            }else{ //?????? ???????????? ??????????????? ??????
                goalMemberRepository.deleteGoalMemberInQuery(userId,goalId);//?????? ????????? ??????
            }

        }catch (Exception e){
            throw new BaseException(FAILED_TO_DELETE_GOAL);
        }
    }

    /**
     * ?????? ??????
     * @param userId
     * @param modifyGoalreqDTO
     * @throws BaseException
     */
    public void modifyGoal(Long userId, ModifyGoalReqDTO modifyGoalreqDTO) throws BaseException {
        checkManager(userId,modifyGoalreqDTO.getGoalId());
        Goal find = goalRepository.findById(modifyGoalreqDTO.getGoalId()).orElseThrow(()-> new BaseException(NOT_EXIST_GOAL));
        //goal??? ?????? ??????-> ?????? ??????
        try{
            find.modifyGoal(modifyGoalreqDTO.getTitle(),OpenCategory.valueOf(modifyGoalreqDTO.getOpenFlag()));
            goalRepository.save(find);//update??????
        }catch(Exception e){
            throw new BaseException(FAILED_TO_MODIFY_GOAL);
        }
    }

    /**
     * ?????? ??????/??????
     * @param goalId
     * @param userId
     * @param accept
     * @throws BaseException
     */
    public void acceptGroupGoal(Long goalId,Long userId, int accept) throws BaseException {
        //????????? ????????? ??????????????? ??????
        GoalMember findGoalMember = goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(()->new BaseException(INVALID_GOAL_USER));
        if(findGoalMember.getStatus()==ACCEPT){
            throw new BaseException(FAILED_TO_ACCEPT_GOAL); //?????? ?????? ????????? ??????
        }
        try{
            if(accept==1){ //1?????? ??????, 0?????? ??????
                findGoalMember.accept();
                goalMemberRepository.save(findGoalMember);

                //????????? ?????????
                sendGoalAcceptMessage(userId,findGoalMember.getGoal());
            }else{
                goalMemberRepository.delete(findGoalMember); //????????? ???????????? ???
                notificationService.confirmGroupReqNotification(userId, findGoalMember.getGoal().getGoalId()); //??????????????? ??????
            }

        }catch(Exception e){
            throw new BaseException(FAILED_TO_ACCEPT_GOAL);
        }
    }

    public void sendGoalAcceptMessage(Long userId, Goal goal) throws BaseException {
        List<GoalMember> goalMembers = goalMemberRepository.findGoalMembersByGoal(goal.getGoalId());
        notificationService.confirmGroupReqNotification(userId, goal.getGoalId());
        User user = userService.findUser(userId);
        for (GoalMember goalMember : goalMembers) {
            if(goalMember.getMember().getUserId()==userId) continue; //?????? ??????!
            //notification ????????? ??????
            notificationService.createNotification(goalMember.getMember(),GROUP_REQUEST,
                    user.getNickname()+" ??????"+ goal.getTitle()+" ??????????????? ?????????????????????.",null,goal,null);
            //fcm?????? ??????
            try{
                List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_groupFlag1(goalMember.getMember());
                firebaseCloudMessageService.sendMessageTo(deviceTokenList,"?????? ?????? ??????",
                        "??????????????? ?????? ??? ????????? ??????????????????. ????????? ??????????????????!"
                );
            }catch(BaseException e){
                log.error("[FCM ?????? ??????] " + e.getStatus());
            }
        }
    }

    /**
     * ?????? ????????? ??????
     * @param userId
     * @param goalId
     * @return
     * @throws BaseException
     */
    public boolean checkManager(Long userId, Long goalId) throws BaseException {
        if(goalMemberRepository.checkMemberRole(userId,goalId)!=GoalMemberRole.MANAGER){
            throw new BaseException(NOT_GOAL_MANAGER);
        }
        return true;
    }

    /**
     * ?????? ?????? ???????????? ??????
     * @param goalId
     * @return
     * @throws BaseException
     */
    public Goal getGoal(Long goalId) throws BaseException {
        return goalRepository.findById(goalId).orElseThrow(()->new BaseException(NOT_EXIST_GOAL));
    }

    /**
     * ?????? ?????? ???????????? ?????? (?????? ?????????, ?????? ??????)
     * @param goalId
     * @return
     */
    public List<GoalMember> getGoalMembers(Long goalId){
        return goalMemberRepository.findGoalMembersByGoal(goalId);
    }

    //?????? ?????? ????????? ???????????? ??????
    public GoalMember getGoalMember(Long goalId,Long userId) throws BaseException {
        return goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(()->new BaseException(INVALID_GOAL_USER));
    }

    /**
     * ?????? ?????? ?????? ??? ?????? ???????????? api
     * @param userId
     * @param nickname
     * @return
     */
    public List<GoalSearchUserResDTO> goalSearchUser(Long userId, String nickname) throws BaseException {
        //??????????????? ?????? DTO ????????????
        List<User> friends = friendService.getSearchFriends(userId,nickname);

        List<GoalSearchUserResDTO> responses = new ArrayList<>();
        for (User user : friends) {
            responses.add(new GoalSearchUserResDTO(user.getUserId(),user.getNickname(),user.getProfileColor().toString()));
        }
        return responses;
    }

    //setter?????? ???????????? ?????? ??????
    public GetGoalDetailResDTO getGoalDetail(Long userId, Long goalId) throws BaseException {
        GetGoalDetailResDTO getGoalDetailResDTO = new GetGoalDetailResDTO();
        //goal??? ???????????? ?????? ????????? ???????????? ?????? (?????? ??????)
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //DTO??? ??? ?????? (goalId, title)
        getGoalDetailResDTO.setGoalId(goal.getGoalId());
        getGoalDetailResDTO.setGoalTitle(goal.getTitle());
        if(goal.getOpenFlag()==OpenCategory.PUBLIC){
            getGoalDetailResDTO.setOpenFlag(true);
        }else{
            getGoalDetailResDTO.setOpenFlag(false);
        }
        //goalMemberList (accept ?????????) -> ?????? ?????? ????????? ?????? ???????????? ???
//        List<GoalMember> goalMembers = getGoalMembers(goalId)
//                .stream().filter(m->m.getStatus()==ACCEPT)
//                .collect(Collectors.toList());
        List<GoalMember> goalMembers = getGoalMembers(goalId);

        int goalPercentage=0;
        //goalMember DTO ?????????
        List<GetGoalMemberDetailDTO> goalMemberDetails = new ArrayList<>();
        for (GoalMember goalMember : goalMembers) {
            GetGoalMemberDetailDTO getGoalMemberDetailDTO = new GetGoalMemberDetailDTO();
            getGoalMemberDetailDTO.setUserId(goalMember.getMember().getUserId());
            getGoalMemberDetailDTO.setNickname(goalMember.getMember().getNickname());
            getGoalMemberDetailDTO.setProfileColor(goalMember.getMember().getProfileColor().toString());
            getGoalMemberDetailDTO.setManagerFlag(goalMember.getMemberRole().toString().equals("MANAGER")?true:false);
            getGoalMemberDetailDTO.setWaitFlag(goalMember.getStatus().toString().equals("WAIT")?true:false);
            //?????? ?????? ????????? ???????????? (????????????)
            List<TodoMember> todoMembers = goalMember.getTodoMembers().stream().filter(m->m.getUpdateAt().toLocalDate().equals(LocalDate.now())).collect(Collectors.toList());
            int completeCount = todoMembers.stream().filter(m->m.getCompleteFlag()==CompleteFlag.COMPLETE)
                    .collect(Collectors.toList()).size();
            //todoMember DTO ????????? ?????????
            List<GetTodoMemberDTO> getTodoMembers = new ArrayList<>();
            for (TodoMember todoMember : todoMembers) {
                GetTodoMemberDTO getTodoMemberDTO = new GetTodoMemberDTO();
                getTodoMemberDTO.setTodoMemberId(todoMember.getTodoMemberId());
                getTodoMemberDTO.setTodoTitle(todoMember.getTodo().getTitle());
                getTodoMemberDTO.setCompleteFlag(todoMember.getCompleteFlag().toString().equals("COMPLETE")?true:false);

                List<User> todoMemberLikes = todoMember.getTodoMemberLikes().stream().map(TodoMemberLike::getUser).collect(Collectors.toList());
                getTodoMemberDTO.setLikeCount(todoMemberLikes.size()); //????????? ??????
                //?????? ??????????????? ??????
                if(todoMemberLikes.stream().filter(m->m.getUserId().equals(userId)).count()!=0){
                    getTodoMemberDTO.setLikeFlag(true);
                }

                getTodoMembers.add(getTodoMemberDTO);
            }
            int percentage = todoMembers.size()==0?0:completeCount*100/ todoMembers.size();
            goalPercentage+=percentage;
            //????????? ??????
            getGoalMemberDetailDTO.setPercentage(percentage);
            getGoalMemberDetailDTO.setGetTodoMembers(getTodoMembers);
            //DTO ???????????? ?????? ??????
            goalMemberDetails.add(getGoalMemberDetailDTO);
        }
        goalPercentage = goalMembers.size()==0?0:goalPercentage/goalMembers.size();
        getGoalDetailResDTO.setGoalPercentage(goalPercentage);
        getGoalDetailResDTO.setGoalMemberDetails(goalMemberDetails);
        return getGoalDetailResDTO;
    }


    //??? ?????? ??????
    public List<GetGoalMainInfoResDTO> getGoalMainInfo(Long targetUserId) throws BaseException {
        //?????? ?????? (?????? ??? ????????? X)
        List<GoalMember> targetGoalMembers = goalMemberRepository.findGoalMembersByMember(targetUserId)
                .stream().filter(m->m.getStatus()==ACCEPT && m.getGoal().getGoalStatus()==ACTIVE)
                .collect(Collectors.toList());
        //????????? ??????
        if(targetGoalMembers.isEmpty() || targetGoalMembers==null){
            return new ArrayList<>();
        }
        List<GetGoalMainInfoResDTO> goalMainInfoResList = new ArrayList<>();
        for (GoalMember targetGoalMember : targetGoalMembers) {
            //todoMember ?????? (????????? ??????)
            List<TodoMember> todoMembers = targetGoalMember.getTodoMembers()
                    .stream().filter(m->m.getUpdateAt().toLocalDate().isEqual(LocalDate.now()))
                    .collect(Collectors.toList());
            int completeCount = todoMembers.stream().filter(m->m.getCompleteFlag()==CompleteFlag.COMPLETE)
                    .collect(Collectors.toList()).size();
            List<GetTodoMainResDTO> todoMainResList = new ArrayList<>();
            //boolean likeFlag = targetGoalMember.getGoal().getOpenFlag().toString().equals("PUBLIC")?true:false;
            for (TodoMember todoMember : todoMembers) {
                boolean completeFlag = todoMember.getCompleteFlag() == CompleteFlag.COMPLETE ? true : false;
                todoMainResList.add(
                        new GetTodoMainResDTO(
                                todoMember.getTodoMemberId(),
                                todoMember.getTodo().getTitle(),
                                completeFlag,
                                todoMember.getTodoMemberLikes().size(),
                                false
                        ));
            }
            boolean groupFlag = targetGoalMember.getGoal().getGroupFlag()==GroupCategory.GROUP?true:false;
            boolean managerFlag = targetGoalMember.getMemberRole() == MANAGER ? true : false;
            boolean openFlag = targetGoalMember.getGoal().getOpenFlag()==OpenCategory.PUBLIC?true:false;
            int percentage = todoMembers.size()==0?0:100*completeCount/todoMembers.size();
            goalMainInfoResList.add(new GetGoalMainInfoResDTO(
                    targetGoalMember.getGoal().getGoalId(),
                    targetGoalMember.getGoal().getTitle(),
                    groupFlag,
                    percentage,
                    managerFlag,
                    openFlag,
                    todoMainResList
            ));
        }
        return goalMainInfoResList;
    }

    //?????? ?????? ??????
    public List<GetGoalMainInfoResDTO> getFriendGoalMainInfo(Long userId,Long targetUserId) throws BaseException {
        //1. target!=user??? ???????????? ??????
        if (userId != targetUserId) {
            User myUser = userService.findUser(userId);
            User targetUser = userService.findUser(targetUserId);
            if (friendService.isFriend(myUser, targetUser) == false) {
                throw new BaseException(NOT_FRIEND_RELATION);
            }
        }
        //????????? ??????
        //?????? ?????? (??????, ??????, ????????? X)
        List<GoalMember> targetGoalMembers = goalMemberRepository.findGoalMembersByMember(targetUserId)
                .stream().filter(m->m.getGoal().getOpenFlag()==OpenCategory.PUBLIC && m.getGoal().getGoalStatus()==ACTIVE&&m.getStatus()==ACCEPT)
                .collect(Collectors.toList());
        //????????? ??????
        if(targetGoalMembers.isEmpty() || targetGoalMembers==null){
            return null;
        }
        List<GetGoalMainInfoResDTO> goalMainInfoResList = new ArrayList<>();
        for (GoalMember targetGoalMember : targetGoalMembers) {
            //todoMember ?????? (????????? ?????? ??????)
            List<TodoMember> todoMembers = targetGoalMember.getTodoMembers()
                    .stream().filter(m->m.getUpdateAt().toLocalDate().isEqual(LocalDate.now()))
                    .collect(Collectors.toList());
            int completeCount = todoMembers.stream().filter(m->m.getCompleteFlag()==CompleteFlag.COMPLETE)
                    .collect(Collectors.toList()).size();
            List<GetTodoMainResDTO> todoMainResList = new ArrayList<>();
            for (TodoMember todoMember : todoMembers) {
                boolean likeFlag = todoMember.getTodoMemberLikes().
                        stream().filter(m->m.getUser().getUserId().equals(userId)).count()!=0?true:false; //?????? ???????????? ???????????? ??????
                boolean completeFlag = todoMember.getCompleteFlag() == CompleteFlag.COMPLETE ? true : false;
                todoMainResList.add(
                new GetTodoMainResDTO(
                                todoMember.getTodoMemberId(),
                                todoMember.getTodo().getTitle(),
                                completeFlag,
                                todoMember.getTodoMemberLikes().size(),
                                likeFlag
                        ));
            }
            boolean groupFlag = targetGoalMember.getGoal().getGroupFlag()==GroupCategory.GROUP?true:false;
            boolean managerFlag = targetGoalMember.getMemberRole() == MANAGER ? true : false;
            boolean openFlag = targetGoalMember.getGoal().getOpenFlag()==OpenCategory.PUBLIC?true:false;
            int percentage = todoMembers.size()==0?0:100*completeCount/todoMembers.size();
            goalMainInfoResList.add(new GetGoalMainInfoResDTO(
                    targetGoalMember.getGoal().getGoalId(),
                    targetGoalMember.getGoal().getTitle(),
                    groupFlag,
                    percentage,
                    managerFlag,
                    openFlag,
                    todoMainResList
            ));
        }
        return goalMainInfoResList;
    }

    //?????? ??????????????? ?????????
    public void changeToArchive(Long userId, Long goalId) throws BaseException {
        //?????? ?????? ????????????
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //??????????????? ?????? -> ????????? exception
        checkManager(userId,goalId);
        try{
            goal.archiveGoal();
            goalRepository.save(goal);
        }catch (Exception e){
            throw new BaseException(FAILED_TO_ARCHIVE_GOAL);
        }
    }

    //?????? ????????? ????????? ??????
    public List<GetArchiveGoalResDTO> getArchiveGoals(Long userId) throws BaseException {
        try {
            List<Goal> archiveGoals = goalMemberRepository.getGoalArchivesByMember(userId);
            List<GetArchiveGoalResDTO> response = new ArrayList<>();
            for (Goal archiveGoal : archiveGoals) {
                response.add(new GetArchiveGoalResDTO(archiveGoal.getGoalId(),archiveGoal.getTitle()));
            }
            return response;
        }catch(Exception e){
            throw new BaseException(FAILED_TO_GET_ARCHIVES);
        }
    }

    //?????? ???????????? ?????? ??? ?????????
    public void archiveToActive(Long goalId) throws BaseException {
        //goal?????? ??????
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        if(goal.getGoalStatus()!=ARCHIVE){
            throw new BaseException(NOT_ARCHIVE_GOAL);
        }
        try{
            goal.activateGoal();
            goalRepository.save(goal);
        }catch(Exception e){
            throw new BaseException(FAILED_TO_ACTIVATE_GOAL);
        }
    }

    public GetAcceptGoalResDTO getAcceptGoal(Long userId, Long goalId) throws BaseException {
        //??????
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));

        //???????????? ??????
        GoalMember checkMember = goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(() -> new BaseException(INVALID_GOAL_USER));

        //?????? ?????? ????????? ??????
        if(checkMember.getStatus()!=WAIT){
            throw new BaseException(ALREADY_ACCEPT_GOAL);
        }

        try {
            List<GoalMember> goalMembersByGoal = goalMemberRepository.findGoalMembersByGoal(goalId);

            List<GetGoalMemberInfoDTO> goalMemberResList = new ArrayList<>();
            for (GoalMember goalMember : goalMembersByGoal) {
                goalMemberResList.add(new GetGoalMemberInfoDTO(goalMember.getMember().getNickname()
                        ,goalMember.getMember().getProfileColor().toString(),goalMember.getMemberRole()==MANAGER?true:false));
            }

            return new GetAcceptGoalResDTO(goal.getTitle(),goalMemberResList);
        }catch (Exception e){
            throw new BaseException(FAILED_TO_GET_GOAL_INFO);
        }

    }

    public void deleteGoals(Long userId, GoalMemberRole role) throws BaseException{
        try{
            List<Goal> goals = goalMemberRepository.getGoalsByUserIdAndRole(userId, role);
            for(Goal goal : goals){
                goalRepository.deleteGoalByIdInDB(goal.getGoalId());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("getGoalsByUserIdAndRole() : goalMemberRepository.getGoalsByUserIdAndRole(userId, role) ?????? ??? ?????????????????? ?????? ?????????");
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
