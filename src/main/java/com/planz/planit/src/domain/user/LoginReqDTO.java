package com.planz.planit.src.domain.user;

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
