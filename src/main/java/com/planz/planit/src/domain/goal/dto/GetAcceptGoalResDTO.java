package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetAcceptGoalResDTO {
    private String title;
    private List<GetGoalMemberInfoDTO> goalMemberList = new ArrayList<>();
}
