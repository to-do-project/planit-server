package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class GetGoalMainInfoResDTO {
    private Long goalId;
    private String goalTitle;
    private boolean groupFlag;
    private int percentage;
    private boolean managerFlag;
    private boolean openFlag;
    private boolean missionFlag;
    private List<GetTodoMainResDTO> getTodoMainResList;
}
