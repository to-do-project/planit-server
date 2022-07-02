package com.planz.planit.src.domain.item.dto;

import com.planz.planit.src.domain.item.ItemCategory;
import com.planz.planit.src.domain.item.ItemType;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Builder
public class GetItemInfoResDTO {

    // 아이템 정보
    private Long itemId;
    private String name;
    private String description;
    private int price;

    // 캐릭터 아이템 or 행성 아이템
    private String type;

    // 구매할 수 있는 최소, 최대 개수
    private int minCnt;
    private int maxCnt;


}
