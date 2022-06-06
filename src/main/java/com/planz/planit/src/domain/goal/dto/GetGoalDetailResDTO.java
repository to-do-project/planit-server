package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetGoalDetailResDTO {
    private Long goalId;
    private String goalTitle;
    private int goalPercentage;
    private List<GetGoalMemberDetailDTO> goalMemberDetails = new ArrayList<>();
}
