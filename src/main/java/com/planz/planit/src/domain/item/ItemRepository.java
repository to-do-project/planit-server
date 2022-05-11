package com.planz.planit.src.domain.item;

import com.planz.planit.src.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByItemId(Long itemId);
}
