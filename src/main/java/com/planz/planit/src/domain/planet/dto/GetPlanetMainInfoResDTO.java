package com.planz.planit.src.domain.planet.dto;

import com.planz.planit.src.domain.inventory.dto.ItemPositionDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GetPlanetMainInfoResDTO {

    // 행성 레벨
    private Integer level;

    // 현재 사용중인 캐릭터 아이템 아이디
    private Long characterItem;

    // 현재 사용중인 행성 아이템 아이디 + 위치
    private List<ItemPositionDTO> planetItemList;
}
