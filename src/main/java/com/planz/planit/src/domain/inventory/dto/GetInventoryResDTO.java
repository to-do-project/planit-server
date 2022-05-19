package com.planz.planit.src.domain.inventory.dto;

import com.planz.planit.src.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetInventoryResDTO {
    private Long itemId;
    private int count;
}
