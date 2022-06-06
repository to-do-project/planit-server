package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinReqDTO {

    @NotBlank(message = "NOT_EXIST_JOIN_REQ_DTO")
    @Size(max = 30, message = "INVALID_EMAIL_FORM")
    @Pattern(regexp = "^[A-Za-z0-9._^%$~#+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "INVALID_EMAIL_FORM")
    private String email;

    @NotBlank(message = "NOT_EXIST_JOIN_REQ_DTO")
    @Size(min = 6, max = 15, message = "INVALID_PWD_FORM")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,15}$", message = "INVALID_PWD_FORM")
    private String password;

    @NotBlank(message = "NOT_EXIST_JOIN_REQ_DTO")
    @Size(max = 8, message = "INVALID_NICKNAME_FORM")
    @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣]{1,8}$", message = "INVALID_NICKNAME_FORM")
    private String nickname;

    @NotBlank(message = "NOT_EXIST_JOIN_REQ_DTO")
    @Pattern(regexp = "^(RED|GREEN|BLUE)$", message = "INVALID_PLANET_COLOR_FORM")
    private String planetColor;

    @NotBlank(message = "NOT_EXIST_JOIN_REQ_DTO")
    private String deviceToken;
}
