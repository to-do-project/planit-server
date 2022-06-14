package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetArchiveGoalResDTO {
    private Long goalId;
    private String title;
}
