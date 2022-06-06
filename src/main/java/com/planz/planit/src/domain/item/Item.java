package com.planz.planit.src.domain.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    private String code;
    private String name;
    private String description;

    // 아이템 가격
    private int price;

    // 배치 최대 수량
    @Column(name = "max_cnt")
    private int maxCnt;

    // 캐릭터 아이템 or 행성 아이템
    @Enumerated(EnumType.STRING)
    private ItemType type;

    // 종류 + 종류 코드
    // 캐릭터 옷, 기본 건축물, 식물, 길, 바위, 기타
    @Enumerated(EnumType.STRING)
    private ItemCategory category;

}
