package com.planz.planit.src.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlacePlanetItemsReqDTO {

    @NotEmpty(message = "NOT_EXIST_ITEM_POSITION_LIST")
    @Valid
    private List<ItemPositionDTO> itemPositionList;

}
