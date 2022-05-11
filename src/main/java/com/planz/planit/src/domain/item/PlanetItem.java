package com.planz.planit.src.domain.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@NoArgsConstructor
@Entity
public class PlanetItem extends Item{

    @Column(name = "max_cnt")
    private int maxCnt;

    @Enumerated(EnumType.STRING)
    private PlanetItemCategory category;

    @Builder
    public PlanetItem(String code, String name, String description, int price, int maxCnt, PlanetItemCategory category) {
        super(code, name, description, price);
        this.maxCnt = maxCnt;
        this.category = category;
    }
}
