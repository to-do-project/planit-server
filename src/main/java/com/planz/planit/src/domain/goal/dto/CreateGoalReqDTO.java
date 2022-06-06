package com.planz.planit.src.domain.goal.dto;

import com.planz.planit.src.domain.user.dto.SearchUserResDTO;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoalReqDTO {

    @NotBlank(message = "NOT_EXIST_GOAL_TITLE")
    @Size(max=20,message = "INVALID_GOAL_TITLE")
    private String title;
    @NotBlank(message="NOT_EXIST_GOAL_OPEN_FLAG")
    @Pattern(regexp = "^PRIVATE|PUBLIC$", message = "INVALID_GOAL_OPEN_FLAG")
    private String openFlag;
    @NotBlank
    @Pattern(regexp = "^PERSONAL|GROUP$", message = "INVALID_GOAL_GROUP_FLAG")
    private String groupFlag;
    private List<Long> memberList = new ArrayList<>();
}
