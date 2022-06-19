package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalSearchUserResDTO {
    private Long userId;
    private String nickname;
    private String profileColor;
}
