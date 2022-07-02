package com.planz.planit.src.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyPlanetItemReqDTO {

    @NotNull(message = "NOT_EXIST_ITEM_ID")
    private Long itemId;

    @NotNull(message = "NOT_EXIST_ITEM_COUNT")
    @Min(value = 1, message = "INVALID_ITEM_COUNT")
    private Integer count;

}
