package com.planz.planit.src.domain.friend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AcceptReqDTO {
    private Long friendId;
    private boolean accepted;
}
