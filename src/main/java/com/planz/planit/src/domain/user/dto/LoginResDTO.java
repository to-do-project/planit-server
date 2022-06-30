package com.planz.planit.src.domain.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResDTO {

    private Long userId;

    private Long planetId;

    private Integer planetLevel;

    private String planetColor;

    private String email;

    private String nickname;

    private Long characterItem;

    private String profileColor;

    private Integer point;

    private Integer missionStatus;

    private String deviceToken;

    private Integer exp;
}
