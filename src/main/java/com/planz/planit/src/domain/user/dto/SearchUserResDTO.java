package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SearchUserResDTO {
    private Long userId;
    private String nickname;
    private String profileColor;
    private int planetLevel;
}
