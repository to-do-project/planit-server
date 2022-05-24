package com.planz.planit.src.domain.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor  // requestDTO에 필수!
@AllArgsConstructor // requestDTO에 필수!
@Builder
@Getter
public class BuyItemReqDTO {

    @NotNull(message = "NOT_EXIST_ITEM_ID")
    private Long itemId;

    // 구매할 개수
    @NotNull(message = "NOT_EXIST_ITEM_COUNT")
    @Min(value = 1, message = "INVALID_ITEM_COUNT")
    private Integer count;

    // 지불할 가격
    @NotNull(message = "NOT_EXIST_ITEM_TOTAL_PRICE")
    private Integer totalPrice;
}
