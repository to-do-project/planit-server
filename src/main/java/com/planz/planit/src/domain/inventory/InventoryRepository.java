package com.planz.planit.src.domain.inventory;

import com.planz.planit.src.domain.friend.Friend;
import com.planz.planit.src.domain.item.PlanetItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("select i from Inventory i where i.user.userId = :userId and i.planetItem.category = :category")
    List<Inventory> findInventoryItemsByCategory(@Param("userId") Long userId, @Param("category") PlanetItemCategory category);
}
