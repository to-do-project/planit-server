package com.planz.planit.src.domain.planet.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetPlanetMyInfoResDTO {

    // 행성 나이
    private long age;

    // 행성 레벨
    private int level;

    // 보유 포인트
    private int point;

    // 평균 목표 완성률
    private int avgGoalCompleteRate;

    // 좋아요 받은 수
    private int getFavoriteCount;

    // 좋아요 누른 수
    private int putFavoriteCount;
}
