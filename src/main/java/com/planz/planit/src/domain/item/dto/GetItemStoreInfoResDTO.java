package com.planz.planit.src.domain.item.dto;

import com.planz.planit.src.domain.item.Item;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter // serialize를 위해서 DTO에 getter 필수 !
public class GetItemStoreInfoResDTO {
    
    private String nickname;
    private int point;

    private List<Long> characterItemIdList;
    private List<Long> planetItemIdList;

}
