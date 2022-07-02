package com.planz.planit.src.domain.inventory.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetInventoryResDTO {

    // 인벤에 있는 전체 아이템 개수 (기본 아이템 제외)
    private int totalInventoryItemCount;

    private List<InventoryDTO> inventoryList;


    @Getter
    @Builder
    public static class InventoryDTO{
        private String itemCode;

        // 보유 가능한 개수 (아이템 시트에 설정된 최대 보유 개수)
        private int totalCount;

        // 행성에 배치된 아이템 개수
        private int placedCount;

        // 현재 보유 중인 아이템 전체 개수
        private int remainingCount;
    }
}
