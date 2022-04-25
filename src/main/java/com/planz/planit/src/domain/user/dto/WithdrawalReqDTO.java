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
public class WithdrawalReqDTO {

    @NotBlank(message = "NOT_EXIST_PASSWORD")
    @Size(min = 6, max = 15, message = "INVALID_PWD_FORM")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,15}$", message = "INVALID_PWD_FORM")
    private String password;
}
