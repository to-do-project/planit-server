package com.planz.planit.src.domain.inventory.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetInventoryResDTO {
    private Long itemId;

    // 보유 중인 아이템 전체 개수
    private int totalCount;

    // 행성에 배치된 아이템 개수
    private int placedCount;

    // 엔벤토리에 남아있는 아이템 개수
    private int remainingCount;
}
