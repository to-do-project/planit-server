package com.planz.planit.src.domain.deviceToken.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeFlagReqDTO {
    @Pattern(regexp = "^ALL|FRIEND|GROUP|NOTICE|SETTING$",message="NOT_EXIST_FLAG_ENUM")
    private String flag;
    private String deviceToken;
}
