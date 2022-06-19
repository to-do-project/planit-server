package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetGoalMemberInfoDTO {
    private String nickname;
    private String profileColor;
    private boolean managerFlag;
}
