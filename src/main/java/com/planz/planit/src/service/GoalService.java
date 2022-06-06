package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.goal.*;
import com.planz.planit.src.domain.goal.dto.GetGoalDetailResDTO;
import com.planz.planit.src.domain.goal.dto.GetGoalMemberDetailDTO;
import com.planz.planit.src.domain.goal.dto.ModifyGoalReqDTO;
import com.planz.planit.src.domain.goal.dto.CreateGoalReqDTO;
import com.planz.planit.src.domain.todo.TodoMember;
import com.planz.planit.src.domain.todo.TodoMemberLike;
import com.planz.planit.src.domain.todo.dto.GetTodoMemberDTO;
import com.planz.planit.src.domain.todo.dto.LikeUserResDTO;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.dto.GoalSearchUserResDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.*;
import static com.planz.planit.src.domain.goal.GoalMemberRole.MEMBER;
import static com.planz.planit.src.domain.goal.GroupStatus.ACCEPT;
import static com.planz.planit.src.domain.goal.GroupStatus.WAIT;

@Slf4j
@Service
public class GoalService {
    private final UserService userService;
    private final FriendService friendService;
    private final GoalRepository goalRepository;
    private final GoalMemberRepository goalMemberRepository;

    public GoalService(UserService userService, FriendService friendService, GoalRepository goalRepository, GoalMemberRepository goalMemberRepository) {
        this.userService = userService;
        this.friendService = friendService;
        this.goalRepository = goalRepository;
        this.goalMemberRepository = goalMemberRepository;
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

                }
            }
            return true; //그룹 생성 성공 시 true
        }catch(Exception e){
            throw new BaseException(FAILED_TO_CREATE_GOAL);
        }

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
                goalRepository.deleteById(goalId); //목표 삭제
                //멤버 모두 삭제?
                goalMemberRepository.deleteAllGoalMemberInQuery(goalId);
                //좋아요 개수 업데이트
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
            }else{
                goalMemberRepository.delete(findGoalMember);
            }
        }catch(Exception e){
            throw new BaseException(FAILED_TO_ACCEPT_GOAL);
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
    public GoalSearchUserResDTO goalSearchUser(Long userId, String nickname) throws BaseException {
        //검색하려는 친구 DTO 가져오기
        GoalSearchUserResDTO findUserDTO = userService.goalSearchUsers(nickname);
        //친구인지 확인하기
        if(!friendService.checkFriend(userId, findUserDTO.getUserId())){
            throw new BaseException(NOT_EXIST_FRIEND);
        }
        return findUserDTO;
    }

    //setter에서 생성자로 수정 필요
    public GetGoalDetailResDTO getGoalDetail(Long userId, Long goalId) throws BaseException {
        GetGoalDetailResDTO getGoalDetailResDTO = new GetGoalDetailResDTO();
        //goal이 비공개인 경우 유저가 회원인지 확인 (구현 필요)
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new BaseException(NOT_EXIST_GOAL));
        //DTO에 값 넣기 (goalId, title)
        getGoalDetailResDTO.setGoalId(goal.getGoalId());
        getGoalDetailResDTO.setGoalTitle(goal.getTitle());
        getGoalDetailResDTO.setOpenFlag(goal.getOpenFlag().toString());
        //goalMemberList
        List<GoalMember> goalMembers = getGoalMembers(goalId);

        //goalMember DTO 리스트
        List<GetGoalMemberDetailDTO> goalMemberDetails = new ArrayList<>();
        for (GoalMember goalMember : goalMembers) {
            GetGoalMemberDetailDTO getGoalMemberDetailDTO = new GetGoalMemberDetailDTO();
            getGoalMemberDetailDTO.setGoalMemberId(goalMember.getGoalMemberId());
            getGoalMemberDetailDTO.setNickname(goalMember.getMember().getNickname());

            //투두 멤버 리스트 가져오기 (오늘치만)
            List<TodoMember> todoMembers = goalMember.getTodoMembers().stream().filter(m->m.getUpdateAt().toLocalDate().equals(LocalDate.now())).collect(Collectors.toList());
            //todoMember DTO 리스트 만들기
            List<GetTodoMemberDTO> getTodoMembers = new ArrayList<>();
            for (TodoMember todoMember : todoMembers) {
                GetTodoMemberDTO getTodoMemberDTO = new GetTodoMemberDTO();
                getTodoMemberDTO.setTodoMemberId(todoMember.getTodoMemberId());
                getTodoMemberDTO.setTodoTitle(todoMember.getTodo().getTitle());
                getTodoMemberDTO.setCompleteFlag(todoMember.getCompleteFlag().toString());

                List<User> todoMemberLikes = todoMember.getTodoMemberLikes().stream().map(TodoMemberLike::getUser).collect(Collectors.toList());
                getTodoMemberDTO.setLikeCount(todoMemberLikes.size()); //좋아요 개수
                List<LikeUserResDTO> likeUserResDTOS = new ArrayList<>();
                for (User todoMemberLike : todoMemberLikes) {
                    likeUserResDTOS.add(new LikeUserResDTO(todoMemberLike.getUserId(),todoMemberLike.getNickname(),todoMemberLike.getProfileColor().toString()));
                }
                getTodoMemberDTO.setLikeUsers(likeUserResDTOS); // 좋아요한 리스트

                //내가 체크했는지 확인
                if(todoMemberLikes.stream().filter(m->m.getUserId().equals(userId)).count()!=0){
                    getTodoMemberDTO.setLikeFlag(true);
                }

                getTodoMembers.add(getTodoMemberDTO);
            }
            getGoalMemberDetailDTO.setGetTodoMembers(getTodoMembers);
            //DTO 리스트에 객체 추가
            goalMemberDetails.add(getGoalMemberDetailDTO);
        }
        getGoalDetailResDTO.setGoalMemberDetails(goalMemberDetails);
        return getGoalDetailResDTO;
    }
}
