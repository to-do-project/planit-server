package com.planz.planit.src.domain.inventory.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetInventoryResDTO {
    private Long itemId;
    private int count;
}
