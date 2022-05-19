package com.planz.planit.src.domain.closet;

import com.planz.planit.src.domain.item.Item;
import com.planz.planit.src.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    boolean existsByItemAndUser(Item item, User user);

}
