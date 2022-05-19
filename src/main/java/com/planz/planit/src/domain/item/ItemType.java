package com.planz.planit.src.domain.item;

import lombok.Getter;

@Getter
public enum ItemType {
    CHARACTER_ITEM("캐릭터 아이템"),
    PLANET_ITEM("행성 아이템");

    private final String kName;

    private ItemType(String kName) {
        this.kName = kName;
    }
}
