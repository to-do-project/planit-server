package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModifyNicknameReqDTO {

    @NotBlank(message = "NOT_EXIST_NICKNAME")
    @Size(max = 8, message = "INVALID_NICKNAME_FORM")
    @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣]{1,8}$", message = "INVALID_NICKNAME_FORM")
    private String nickname;

}
