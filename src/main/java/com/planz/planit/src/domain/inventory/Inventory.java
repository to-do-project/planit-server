package com.planz.planit.src.domain.inventory;

import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.item.PlanetItem;
import com.planz.planit.src.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_item_unique_constraint",
                        columnNames = {"user_id", "item_id"}
                )
        }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private PlanetItem planetItem;

    private int count;

    // 값타입 컬랙션
    @ElementCollection
    @CollectionTable(name = "ItemPlacement",
            joinColumns = @JoinColumn(name = "inventory_id"))
    private List<Position> itemPlacement = new ArrayList<>();


}
