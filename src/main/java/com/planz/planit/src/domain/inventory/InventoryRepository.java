package com.planz.planit.src.domain.inventory;

import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.item.ItemCategory;
import com.planz.planit.src.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("select i from Inventory i where i.user.userId = :userId and i.planetItem.category = :category")
    List<Inventory> findInventoryItemsByCategory(@Param("userId") Long userId, @Param("category") ItemCategory category);


  /*  @Query("select " +
                "case when (i.count + :numberOfPurchases) <= i.planetItem.maxCnt " +
                "then true " +
                "else false end " +
            "from Inventory i " +
            "where i.user.userId = :userId and i.planetItem.itemId = :itemId")
    Boolean checkItemMaxCnt(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("numberOfPurchases") int numberOfPurchases);
*/

    Optional<Inventory> findByUserAndPlanetItem(User user, Item planetItem);

    Optional<Inventory> findByInventoryId(Long inventoryId);

    @Query(value = "select i from Inventory i join fetch i.planetItem where i.user = :user")
    List<Inventory> findInventorysAndPlanetItemsByUser(User user);

    List<Inventory> findByUser(User user);


    @Query(value = "select i.count from Inventory i where i.user.userId = :userId and i.planetItem.itemId = :itemId")
    Optional<Integer> findInventoryCount(@Param("userId") Long userId, @Param("itemId") Long itemId);

}
