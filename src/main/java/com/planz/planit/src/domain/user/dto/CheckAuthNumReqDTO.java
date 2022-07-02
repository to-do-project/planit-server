package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckAuthNumReqDTO {

    @NotBlank(message = "NOT_EXIST_EMAIL")
    @Size(max = 30, message = "INVALID_EMAIL_FORM")
    @Pattern(regexp = "^[A-Za-z0-9._^%$~#+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "INVALID_EMAIL_FORM")
    private String email;

    @NotBlank(message = "NOT_EXIST_AUTH_NUM_IN_BODY")
    @Pattern(regexp = "^[0-9]{6}$", message = "INVALID_AUTH_NUM_FORM")
    private String authNum;
}
