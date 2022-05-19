package com.planz.planit.src.domain.item;

import lombok.Getter;

@Getter
public enum ItemCategory {
    cloth("캐릭터 옷", 99),
    basic_architecture("기본 건축물", 0),
    plant("식물 plant", 1),
    road("돌 stone", 2),
    rock("길 road", 3),
    etc("기타", 4);

    private final String kind;
    private final int kindCode;

    private ItemCategory(String kind, int kindCode) {
        this.kind = kind;
        this.kindCode = kindCode;
    }
}
