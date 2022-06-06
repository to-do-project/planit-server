package com.planz.planit.src.domain.friend;

import com.planz.planit.src.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend,Long> {

    @Query(value = "select f from Friend f where f.fromUser.userId = :userId and f.friendStatus <> 'DELETE'")
    public List<Friend> findByFromUser(Long userId);
    @Query(value = "select f from Friend f where f.toUser.userId = :userId and f.friendStatus <> 'DELETE'")
    public List<Friend> findByToUser(Long userId);

    boolean existsByFromUserAndToUserAndFriendStatus(User fromUser, User toUser, FriendStatus friendStatus);
}
