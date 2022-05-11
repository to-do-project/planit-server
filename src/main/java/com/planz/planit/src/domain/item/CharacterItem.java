package com.planz.planit.src.domain.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Getter
@NoArgsConstructor
@Entity
public class CharacterItem extends Item{

    @Builder
    public CharacterItem(String code, String name, String description, int price) {
        super(code, name, description, price);
    }
}
