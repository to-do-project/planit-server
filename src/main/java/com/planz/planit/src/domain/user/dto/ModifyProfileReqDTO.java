package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ModifyProfileReqDTO {

    @NotBlank(message = "NOT_EXIST_PROFILE_COLOR")
    @Pattern(regexp = "^(LightRed|Yellow|Green|SkyBlue|Blue|LightPurple|Purple|Pink|Gray|Black)$", message = "INVALID_PROFILE_COLOR")
    private String profileColor;
}
