package com.planz.planit.src.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByuserId(Long userId);


    @Transactional
    @Modifying
    @Query("delete from User u where u.userId is :userId")
    void deleteByUserIdInQuery(@Param("userId") Long userId);

}
