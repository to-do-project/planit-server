package com.planz.planit.src.domain.item;

import lombok.Getter;

@Getter
public enum BasicItem {
    SPACESUIT_01(1L),
    HOUSE_01(8L),
    PORTAL_00(12L);

    private final Long itemId;

    BasicItem(Long itemId) {
        this.itemId = itemId;
    }
}
