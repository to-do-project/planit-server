package com.planz.planit.src.domain.planet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PlanetRepository extends JpaRepository<Planet, Long> {

    @Transactional
    @Modifying
    @Query("delete from Planet p where p.user.userId is :userId")
    void deleteByUserIdInQuery(@Param("userId") Long userId);

    @Query("select p from Planet p where p.user.userId is :userId")
    Optional<Planet> findByUserId(@Param("userId") Long userId);

}
