package com.planz.planit.src.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionDTO {

    @NotNull(message = "NOT_EXIST_POS_X")
    private Float posX;

    @NotNull(message = "NOT_EXIST_POS_Y")
    private Float posY;
}
