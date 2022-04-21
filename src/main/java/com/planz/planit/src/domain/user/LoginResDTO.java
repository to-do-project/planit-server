package com.planz.planit.src.domain.user;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResDTO {

    private Long userId;

    private Long planetId;

    private String email;

    private String nickname;

    private Integer characterColor;

    private Integer profileColor;

    private Integer point;

    private Integer missionStatus;

    private String deviceToken;
}
