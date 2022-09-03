package com.planz.planit.src.domain.planet.dto;

import com.planz.planit.src.domain.inventory.dto.ItemPositionDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GetPlanetMainInfoResDTO {

    // 유저 아이디
    private Long userId;

    // 행성 색깔
    private String planetColor;

    // 행성 레벨
    private Integer level;

    // 전날 투두 완료 퍼센테이지 = 전날 목표량
    private Integer prePercent;

    // 캐릭터 가출 여부
    private Boolean isRunAway;

    // 현재 사용중인 캐릭터 아이템 아이디
    private Long characterItem;

    // 현재 사용중인 행성 아이템 아이디 + 위치
    private List<ItemPositionDTO> planetItemList;
}
