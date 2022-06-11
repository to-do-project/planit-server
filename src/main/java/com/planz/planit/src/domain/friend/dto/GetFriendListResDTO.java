package com.planz.planit.src.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetFriendListResDTO {
    private List<GetFriendResDTO> waitFriends;
    private List<GetFriendResDTO> friends;
}
