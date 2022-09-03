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
import static com.planz.planit.src.domain.goal.GoalStatus.*;
import static com.planz.planit.src.domain.goal.GroupCategory.MISSION;
import static com.planz.planit.src.domain.goal.GroupStatus.ACCEPT;
import static com.planz.planit.src.domain.goal.GroupStatus.WAIT;
import static com.planz.planit.src.domain.goal.OpenCategory.PUBLIC;
import static com.planz.planit.src.domain.notification.NotificationSmallCategory.GROUP_REQUEST;
import static com.planz.planit.src.domain.user.UserStatus.RUN_AWAY;

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
     * 목표 생성
     * @param userId
     * @param createGoalReqDTO
     * @return
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public boolean createGoal(Long userId, CreateGoalReqDTO createGoalReqDTO) throws BaseException {

        //매니저 객체 가져오기
        User manager = userService.findUser(userId);
        try{
            Goal goal = Goal.builder()
                    .title(createGoalReqDTO.getTitle())
                    .groupFlag(GroupCategory.valueOf(createGoalReqDTO.getGroupFlag()))
                    .openFlag(OpenCategory.valueOf(createGoalReqDTO.getOpenFlag()))
                    .build();

            //목표 추가
            goalRepository.save(goal);
            //목표 생성자 - goalMember에 매니저 넣기
            GoalMember goalManager = GoalMember.builder()
                    .goal(goal)
                    .member(manager)
                    .status(ACCEPT) //매니저의 경우 무조건 accept
                    .memberRole(GoalMemberRole.MANAGER)
                    .build();

            goalMemberRepository.save(goalManager);

            //그룹이면 그룹 멤버 추가
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

                    //notification 테이블 추가
                    notificationService.createNotification(user,GROUP_REQUEST,
                            createGoalReqDTO.getTitle()+" 그룹목표에 초대받았습니다.",null,goal,null);
                    //fcm알림 추가
                    try{
                        List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_groupFlag1(goalMember.getMember());
                        firebaseCloudMessageService.sendMessageTo(deviceTokenList,"그룹 초대 요청"
                                ,"새로운 그룹목표에 초대받았습니다. 빨리 행성을 확인해주세요!"
                        );
                    }catch(BaseException e){
                        log.error("[FCM 전송 실패] " + e.getStatus());
                    }
                }
            }

        }catch(Exception e){
            throw new BaseException(FAILED_TO_CREATE_GOAL);
        }
        return true; //그룹 생성 성공 시 true
    }
    /**
     * 회원가입 후 자동으로 생성되는 운영자 매일 미션 함수
     * @param userId
     * @return goalId
     */
    public Long createMissionGoal(Long userId) throws BaseException {
        //유저 찾기
        User user = userService.findUser(userId);
        //운영자 목표 생성
        try{
            Goal goal = Goal.builder()
                    .title("매일 미션")
                    .groupFlag(MISSION)
                    .openFlag(PUBLIC)
                    .build();
            //목표 추가
            goalRepository.save(goal);

            //사용자 - goalMember에 멤버로 넣기
            GoalMember goalManager = GoalMember.builder()
                    .goal(goal)
                    .member(user)
                    .status(ACCEPT) //매니저의 경우 무조건 accept
                    .memberRole(GoalMemberRole.MEMBER)
                    .build();

            goalMemberRepository.save(goalManager);

            //성공 시 목표 id를 반환한다.
            return goal.getGoalId();
        }catch(Exception e) {
            throw new BaseException(FAILED_TO_CREATE_GOAL);
        }
    }

    /**
     * 매일 미션 기능 끄기 (예외처리 필요)
     * @param userId
     * @return
     */
    public boolean changeToArchiveMission(Long userId) throws BaseException {
        try {
            Goal goalByMissionAndUser = goalMemberRepository.getGoalByMissionAndUser(userId);

            if (goalByMissionAndUser.getGoalStatus() == ACTIVE) {
                goalByMissionAndUser.archiveGoal();
            } else {
                goalByMissionAndUser.activateGoal();
            }
            goalRepository.save(goalByMissionAndUser); //저장
        }catch(Exception e){
            throw new BaseException(FAILED_TO_ARCHIVE_GOAL);
        }
            return true;
    }


    /**
     * 목표 삭제 / 탈퇴
     * @param userId
     * @param goalId
     * @throws BaseException
     */
    @Transactional(rollbackFor = {Exception.class, BaseException.class})
    public void deleteGoal(Long userId, Long goalId) throws BaseException {
        //투두 기록은 삭제하지 않음
        try{
            //매니저인지 확인
            if(goalMemberRepository.checkMemberRole(userId,goalId)==GoalMemberRole.MANAGER){
                goalRepository.deleteGoalById(goalId); //목표 삭제
                //멤버 모두 삭제?
                goalMemberRepository.deleteAllGoalMemberInQuery(goalId);

            }else{ //일반 멤버라면 그룹에서만 탈퇴
                goalMemberRepository.deleteGoalMemberInQuery(userId,goalId);//해당 멤버만 삭제
            }

        }catch (Exception e){
            throw new BaseException(FAILED_TO_DELETE_GOAL);
        }
    }

    /**
     * 목표 수정
     * @param userId
     * @param modifyGoalreqDTO
     * @throws BaseException
     */
    public void modifyGoal(Long userId, ModifyGoalReqDTO modifyGoalreqDTO) throws BaseException {
        checkManager(userId,modifyGoalreqDTO.getGoalId());
        Goal find = goalRepository.findById(modifyGoalreqDTO.getGoalId()).orElseThrow(()-> new BaseException(NOT_EXIST_GOAL));
        //goal의 상태 확인-> 에러 처리
        try{
            find.modifyGoal(modifyGoalreqDTO.getTitle(),OpenCategory.valueOf(modifyGoalreqDTO.getOpenFlag()));
            goalRepository.save(find);//update하기
        }catch(Exception e){
            throw new BaseException(FAILED_TO_MODIFY_GOAL);
        }
    }

    /**
     * 목표 수락/거절
     * @param goalId
     * @param userId
     * @param accept
     * @throws BaseException
     */
    public void acceptGroupGoal(Long goalId,Long userId, int accept) throws BaseException {
        //유저가 그룹에 속해있는지 확인
        GoalMember findGoalMember = goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(()->new BaseException(INVALID_GOAL_USER));
        if(findGoalMember.getStatus()==ACCEPT){
            throw new BaseException(FAILED_TO_ACCEPT_GOAL); //이미 요청 수락한 경우
        }
        try{
            if(accept==1){ //1이면 수락, 0이면 거절
                findGoalMember.accept();
                goalMemberRepository.save(findGoalMember);

                //메세지 보내기
                sendGoalAcceptMessage(userId,findGoalMember.getGoal());
            }else{
                goalMemberRepository.delete(findGoalMember); //초기에 삭제해도 됨
                notificationService.confirmGroupReqNotification(userId, findGoalMember.getGoal().getGoalId()); //체크표시는 하기
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
            if(goalMember.getMember().getUserId()==userId) continue; //나는 패스!
            //notification 테이블 추가
            notificationService.createNotification(goalMember.getMember(),GROUP_REQUEST,
                    user.getNickname()+" 님이"+ goal.getTitle()+" 그룹목표에 참가하였습니다.",null,goal,null);
            //fcm알림 추가
            try{
                List<String> deviceTokenList = deviceTokenService.findAllDeviceTokens_groupFlag1(goalMember.getMember());
                firebaseCloudMessageService.sendMessageTo(deviceTokenList,"그룹 초대 수락",
                        "그룹목표에 다른 별 주민이 가입했습니다. 반갑게 인사해주세요!"
                );
            }catch(BaseException e){
                log.error("[FCM 전송 실패] " + e.getStatus());
            }
        }
    }

    /**
     * 목표 매니저 확인
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
     * 목표 객체 가져오는 함수
     * @param goalId
     * @return
     * @throws BaseException
     */
    public Goal getGoal(Long goalId) throws BaseException {
        return goalRepository.findById(goalId).orElseThrow(()->new BaseException(NOT_EXIST_GOAL));
    }

    /**
     * 목표 멤버 가져오는 함수 (멤버 리스트, 단일 멤버)
     * @param goalId
     * @return
     */
    public List<GoalMember> getGoalMembers(Long goalId){
        return goalMemberRepository.findGoalMembersByGoal(goalId);
    }

    //특정 목표 멤버만 가져오는 함수
    public GoalMember getGoalMember(Long goalId,Long userId) throws BaseException {
        return goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(()->new BaseException(INVALID_GOAL_USER));
    }

    /**
     * 그룹 목표 생성 시 친구 검색하는 api
     * @param userId
     * @param nickname
     * @return
     */
    public List<GoalSearchUserResDTO> goalSearchUser(Long userId, String nickname) throws BaseException {
        //검색하려는 친구 DTO 가져오기
        List<User> friends = friendService.getSearchFriends(userId,nickname);

        List<GoalSearchUserResDTO> responses = new ArrayList<>();
        for (User user : friends) {
            responses.add(new GoalSearchUserResDTO(user.getUserId(),user.getNickname(),user.getProfileColor().toString()));
        }
        return responses;
    }

    //setter에서 생성자로 수정 필요
    public GetGoalDetailResDTO getGoalDetail(Long userId, Long goalId) throws BaseException {
        GetGoalDetailResDTO getGoalDetailResDTO = new GetGoalDetailResDTO();
        //goal이 비공개인 경우 유저가 회원인지 확인 (구현 필요)
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //goal이 삭제인 경우 에러 처리
        if(goal.getGoalStatus()==DELETE){
            throw new BaseException(NOT_EXIST_GOAL);
        }

        //DTO에 값 넣기 (goalId, title)
        getGoalDetailResDTO.setGoalId(goal.getGoalId());
        getGoalDetailResDTO.setGoalTitle(goal.getTitle());
        if(goal.getOpenFlag()== PUBLIC){
            getGoalDetailResDTO.setOpenFlag(true);
        }else{
            getGoalDetailResDTO.setOpenFlag(false);
        }
        //goalMemberList (accept 멤버만) -> 대기 중인 멤버도 같이 보여줘야 함
//        List<GoalMember> goalMembers = getGoalMembers(goalId)
//                .stream().filter(m->m.getStatus()==ACCEPT)
//                .collect(Collectors.toList());
        List<GoalMember> goalMembers = getGoalMembers(goalId);

        int goalPercentage=0;
        //goalMember DTO 리스트
        List<GetGoalMemberDetailDTO> goalMemberDetails = new ArrayList<>();
        for (GoalMember goalMember : goalMembers) {
            GetGoalMemberDetailDTO getGoalMemberDetailDTO = new GetGoalMemberDetailDTO();
            getGoalMemberDetailDTO.setUserId(goalMember.getMember().getUserId());
            getGoalMemberDetailDTO.setNickname(goalMember.getMember().getNickname());
            getGoalMemberDetailDTO.setProfileColor(goalMember.getMember().getProfileColor().toString());
            getGoalMemberDetailDTO.setManagerFlag(goalMember.getMemberRole().toString().equals("MANAGER")?true:false);
            getGoalMemberDetailDTO.setWaitFlag(goalMember.getStatus().toString().equals("WAIT")?true:false);
            //투두 멤버 리스트 가져오기 (오늘치만)
            List<TodoMember> todoMembers = goalMember.getTodoMembers().stream().filter(m->m.getUpdateAt().toLocalDate().equals(LocalDate.now())).collect(Collectors.toList());
            int completeCount = todoMembers.stream().filter(m->m.getCompleteFlag()==CompleteFlag.COMPLETE)
                    .collect(Collectors.toList()).size();
            //todoMember DTO 리스트 만들기
            List<GetTodoMemberDTO> getTodoMembers = new ArrayList<>();
            for (TodoMember todoMember : todoMembers) {
                GetTodoMemberDTO getTodoMemberDTO = new GetTodoMemberDTO();
                getTodoMemberDTO.setTodoMemberId(todoMember.getTodoMemberId());
                getTodoMemberDTO.setTodoTitle(todoMember.getTodo().getTitle());
                getTodoMemberDTO.setCompleteFlag(todoMember.getCompleteFlag().toString().equals("COMPLETE")?true:false);

                List<User> todoMemberLikes = todoMember.getTodoMemberLikes().stream().map(TodoMemberLike::getUser).collect(Collectors.toList());
                getTodoMemberDTO.setLikeCount(todoMemberLikes.size()); //좋아요 개수
                //내가 체크했는지 확인
                if(todoMemberLikes.stream().filter(m->m.getUserId().equals(userId)).count()!=0){
                    getTodoMemberDTO.setLikeFlag(true);
                }

                getTodoMembers.add(getTodoMemberDTO);
            }
            int percentage = todoMembers.size()==0?0:completeCount*100/ todoMembers.size();
            goalPercentage+=percentage;
            //퍼센트 추가
            getGoalMemberDetailDTO.setPercentage(percentage);
            getGoalMemberDetailDTO.setGetTodoMembers(getTodoMembers);
            //DTO 리스트에 객체 추가
            goalMemberDetails.add(getGoalMemberDetailDTO);
        }
        goalPercentage = goalMembers.size()==0?0:goalPercentage/goalMembers.size();
        getGoalDetailResDTO.setGoalPercentage(goalPercentage);
        getGoalDetailResDTO.setGoalMemberDetails(goalMemberDetails);
        return getGoalDetailResDTO;
    }


    //내 목표 조회
    public List<GetGoalMainInfoResDTO> getGoalMainInfo(Long targetUserId) throws BaseException {
        //목표 조회 (수락 및 보관함 X)
        List<GoalMember> targetGoalMembers = goalMemberRepository.findGoalMembersByMember(targetUserId)
                .stream().filter(m->m.getStatus()==ACCEPT && m.getGoal().getGoalStatus()==ACTIVE)
                .collect(Collectors.toList());
        //없으면 리턴
        if(targetGoalMembers.isEmpty() || targetGoalMembers==null){
            return new ArrayList<>();
        }
        List<GetGoalMainInfoResDTO> goalMainInfoResList = new ArrayList<>();
        for (GoalMember targetGoalMember : targetGoalMembers) {
            //todoMember 조회 (생성 기준 오늘자 것만)
            List<TodoMember> todoMembers = targetGoalMember.getTodoMembers()
                    .stream().filter(m->m.getTodo().getCreateAt().toLocalDate().isEqual(LocalDate.now()))
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
            boolean missionFlag = targetGoalMember.getGoal().getGroupFlag()== MISSION?true:false;
            boolean managerFlag = targetGoalMember.getMemberRole() == MANAGER ? true : false;
            boolean openFlag = targetGoalMember.getGoal().getOpenFlag()== PUBLIC?true:false;
            int percentage = todoMembers.size()==0?0:100*completeCount/todoMembers.size();
            goalMainInfoResList.add(new GetGoalMainInfoResDTO(
                    targetGoalMember.getGoal().getGoalId(),
                    targetGoalMember.getGoal().getTitle(),
                    groupFlag,
                    percentage,
                    managerFlag,
                    openFlag,
                    missionFlag,
                    todoMainResList
            ));
        }
        return goalMainInfoResList;
    }

    //친구 목표 조회
    public List<GetGoalMainInfoResDTO> getFriendGoalMainInfo(Long userId,Long targetUserId) throws BaseException {
        //1. target!=user면 친구인지 확인
        if (userId != targetUserId) {
            User myUser = userService.findUser(userId);
            User targetUser = userService.findUser(targetUserId);
            if (friendService.isFriend(myUser, targetUser) == false) {
                throw new BaseException(NOT_FRIEND_RELATION);
            }
        }
        //비공개 제외
        //목표 조회 (수락, 공개, 보관함 X)
        List<GoalMember> targetGoalMembers = goalMemberRepository.findGoalMembersByMember(targetUserId)
                .stream().filter(m->m.getGoal().getOpenFlag()== PUBLIC && m.getGoal().getGoalStatus()==ACTIVE&&m.getStatus()==ACCEPT)
                .collect(Collectors.toList());
        //없으면 리턴
        if(targetGoalMembers.isEmpty() || targetGoalMembers==null){
            return null;
        }
        List<GetGoalMainInfoResDTO> goalMainInfoResList = new ArrayList<>();
        for (GoalMember targetGoalMember : targetGoalMembers) {
            //todoMember 조회 (오늘자 투두 조회)
            List<TodoMember> todoMembers = targetGoalMember.getTodoMembers()
                    .stream().filter(m->m.getUpdateAt().toLocalDate().isEqual(LocalDate.now()))
                    .collect(Collectors.toList());
            int completeCount = todoMembers.stream().filter(m->m.getCompleteFlag()==CompleteFlag.COMPLETE)
                    .collect(Collectors.toList()).size();
            List<GetTodoMainResDTO> todoMainResList = new ArrayList<>();
            for (TodoMember todoMember : todoMembers) {
                boolean likeFlag = todoMember.getTodoMemberLikes().
                        stream().filter(m->m.getUser().getUserId().equals(userId)).count()!=0?true:false; //내가 좋아요를 눌렀는지 확인
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
            boolean missionFlag = targetGoalMember.getGoal().getGroupFlag()== MISSION?true:false;
            boolean managerFlag = targetGoalMember.getMemberRole() == MANAGER ? true : false;
            boolean openFlag = targetGoalMember.getGoal().getOpenFlag()== PUBLIC?true:false;
            int percentage = todoMembers.size()==0?0:100*completeCount/todoMembers.size();
            goalMainInfoResList.add(new GetGoalMainInfoResDTO(
                    targetGoalMember.getGoal().getGoalId(),
                    targetGoalMember.getGoal().getTitle(),
                    groupFlag,
                    percentage,
                    managerFlag,
                    openFlag,
                    missionFlag,
                    todoMainResList
            ));
        }
        return goalMainInfoResList;
    }

    //목표 보관함으로 옮기기
    public void changeToArchive(Long userId, Long goalId) throws BaseException {
        //목표 객체 가져오기
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //매니저인지 확인 -> 아니면 exception
        checkManager(userId,goalId);
        try{
            goal.archiveGoal();
            goalRepository.save(goal);
        }catch (Exception e){
            throw new BaseException(FAILED_TO_ARCHIVE_GOAL);
        }
    }

    //목표 보관함 리스트 조회
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

    //목표 보관함에 있던 거 활성화
    public void archiveToActive(Long goalId) throws BaseException {
        //goal상태 확인
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
        //조회
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));

        //멤버인지 확인
        GoalMember checkMember = goalMemberRepository.findGoalMemberByGoalAndUser(goalId, userId).orElseThrow(() -> new BaseException(INVALID_GOAL_USER));

        //이미 요청 했는지 확인
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
            log.error("getGoalsByUserIdAndRole() : goalMemberRepository.getGoalsByUserIdAndRole(userId, role) 실행 중 데이터베이스 에러 발생함");
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
