package com.planz.planit.src.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetUserResultResDTO {
    private int total_exp;
    private int total_point;
}
