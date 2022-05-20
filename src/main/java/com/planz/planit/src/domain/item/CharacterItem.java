package com.planz.planit.src.domain.item;

import lombok.Getter;

@Getter
public enum CharacterItem {
    SPACESUIT_01(1L),
    SPACESUIT_02(2L),
    SPACESUIT_03(3L),
    SPACESUIT_04(4L),
    SPACESUIT_05(5L),
    SPACESUIT_06(6L),
    SPACESUIT_07(7L);

    private final Long itemId;

    CharacterItem(Long itemId) {
        this.itemId = itemId;
    }
}
