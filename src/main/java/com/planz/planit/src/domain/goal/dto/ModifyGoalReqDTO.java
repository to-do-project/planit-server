package com.planz.planit.src.domain.goal.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;


@Getter
@Setter
@NoArgsConstructor
public class ModifyGoalReqDTO {
    @NotNull(message="NOT_EXIST_GOAL_ID")
    @Positive(message="INVALID_GOAL_ID")
    private Long goalId;

    @NotBlank(message="NOT_EXIST_GOAL_TITLE")
    private String title;
    @NotBlank(message="NOT_EXIST_OPEN_FLAG")
    @Pattern(regexp = "^PRIVATE|PUBLIC$", message = "INVALID_GOAL_OPEN_FLAG")
    private String openFlag;
}
