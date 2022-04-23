package com.planz.planit.src.domain.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginReqDTO {

    private String email;
    private String password;
    private String deviceToken;
}
