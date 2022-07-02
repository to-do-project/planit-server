package com.planz.planit.src.domain.deviceToken.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenReqDTO {

    @NotBlank(message = "NOT_EXIST_DEVISE_TOKEN")
    private String deviceToken;
}
