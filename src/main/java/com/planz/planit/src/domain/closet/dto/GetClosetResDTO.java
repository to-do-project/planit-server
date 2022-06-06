package com.planz.planit.src.domain.closet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter     // serialize를 위해서 res DTO에 getter 필수!
public class GetClosetResDTO {

    private List<Long> characterItemIdList = new ArrayList<>();
}
