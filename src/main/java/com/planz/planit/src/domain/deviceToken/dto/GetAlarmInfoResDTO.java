package com.planz.planit.src.domain.deviceToken.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetAlarmInfoResDTO {
    private int allFlag;
    private int friendFlag;
    private int groupFlag;
    private int settingFlag;
    private int noticeFlag;

}
