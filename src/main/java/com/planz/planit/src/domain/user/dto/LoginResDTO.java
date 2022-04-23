package com.planz.planit.src.domain.user.dto;

import com.planz.planit.src.domain.user.UserCharacterColor;
import com.planz.planit.src.domain.user.UserProfileColor;
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

    private String characterColor;

    private String profileColor;

    private Integer point;

    private Integer missionStatus;

    private String deviceToken;
}
