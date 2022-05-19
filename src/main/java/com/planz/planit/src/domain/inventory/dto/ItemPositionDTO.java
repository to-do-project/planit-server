package com.planz.planit.src.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemPositionDTO {

    @NotNull(message = "NOT_EXIST_PLACE_ITEM_ID")
    private Long itemId;

    @NotEmpty(message = "NOT_EXIST_POSITION_LIST")
    @Valid
    private List<PositionDTO> positionList;

}
