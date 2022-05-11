package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.inventory.Inventory;
import com.planz.planit.src.domain.inventory.InventoryRepository;
import com.planz.planit.src.domain.inventory.dto.GetInventoryResDTO;
import com.planz.planit.src.domain.item.PlanetItemCategory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;

@Service
@Log4j2
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<GetInventoryResDTO> getInventoryByCategory(String userId, String category) throws BaseException {
        try {
            List<GetInventoryResDTO> result = new ArrayList<GetInventoryResDTO>();

            List<Inventory> inventoryList = findInventoryItemsByCategory(Long.valueOf(userId), PlanetItemCategory.valueOf(category));

            for (Inventory inventory : inventoryList) {
                result.add(GetInventoryResDTO.builder()
                        .inventoryId(inventory.getInventoryId())
                        .itemId(inventory.getPlanetItem().getItemId())
                        .count(inventory.getCount())
                        .build());
            }

            return result;
        } catch (BaseException e) {
            throw e;
        }
    }

    public List<Inventory> findInventoryItemsByCategory(Long userId, PlanetItemCategory category) throws BaseException {
        try {
            return inventoryRepository.findInventoryItemsByCategory(userId, category);
        } catch (Exception e) {
            log.error("findInventoryItemsByCategory() : inventoryRepository.findInventoryItemsByCategory(userId, category) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
