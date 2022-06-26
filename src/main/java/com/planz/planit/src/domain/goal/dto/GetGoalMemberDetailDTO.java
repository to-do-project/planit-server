package com.planz.planit.src.domain.goal.dto;

import com.planz.planit.src.domain.todo.dto.GetTodoMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetGoalMemberDetailDTO {
    private Long userId;
    private String nickname;
    private int percentage;
    private boolean managerFlag;
    private boolean waitFlag;
    private List<GetTodoMemberDTO> getTodoMembers = new ArrayList<>();
}
