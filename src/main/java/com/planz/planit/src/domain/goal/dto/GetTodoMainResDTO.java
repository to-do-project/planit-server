package com.planz.planit.src.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetTodoMainResDTO {
    private Long todoMemberId;
    private String todoTitle;
    private boolean completeFlag;
    private int likeCount;
    private boolean likeFlag;//내가 눌렀는지
}
