package com.planz.planit.src.domain.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@Getter
@Setter
public class GetLikeTodoResDTO {
    private int likeCount;
    private List<LikeUserResDTO> userInfo = new ArrayList<>();
}
