package com.planz.planit.src.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BuyItemResDTO {

    private Long itemId;

    // 구매할 수 있는 최소, 최대 개수
    private int minCnt;
    private int maxCnt;

    // 현재 보유 포인트
    private int point;
}
