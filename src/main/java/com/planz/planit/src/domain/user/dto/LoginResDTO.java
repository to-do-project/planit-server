package com.planz.planit.src.domain.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResDTO {

    private Long userId;

    private Long planetId;

    private String email;

    private String nickname;

    private Long characterItem;

    private String profileColor;

    private Integer point;

    private Integer missionStatus;

    private String deviceToken;
}
