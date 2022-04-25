package com.planz.planit.src.domain.deviceToken.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenReqDTO {
    private String deviceToken;
}
