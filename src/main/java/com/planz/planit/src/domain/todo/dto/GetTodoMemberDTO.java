package com.planz.planit.src.domain.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetTodoMemberDTO {
    private Long todoMemberId;
    private String todoTitle;
    private boolean completeFlag;
    private int likeCount;
    private boolean likeFlag;//내가 좋아요를 눌렀는지 확인
}
