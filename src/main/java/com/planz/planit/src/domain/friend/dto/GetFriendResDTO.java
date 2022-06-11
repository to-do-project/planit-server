package com.planz.planit.src.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetFriendResDTO {
    private Long userId;
    private String nickName;
    private String profileColor;
    private boolean waitFlag;
}
