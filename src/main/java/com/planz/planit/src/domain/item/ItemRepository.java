package com.planz.planit.src.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "select i from Item i where i.itemId = :itemId and i.type = :type")
    Optional<Item> findItemByIdAndType(@Param("itemId") Long itemId, @Param("type") ItemType type);

    @Query(value = "select i.itemId from Item i where i.type = :type")
    List<Long> findItemIdsByType(@Param("type") ItemType type);

    @Override
    Optional<Item> findById(Long itemId);
}
