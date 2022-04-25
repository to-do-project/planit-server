package com.planz.planit.src.domain.friend;

import com.planz.planit.src.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend,Long> {

    @Query(value = "select f from Friend f where f.fromUser.id = :userId and f.friendStatus <> 'DELETE'")
    public List<Friend> findByFromUserId(Long userId);
    @Query(value = "select f from Friend f where f.toUser.id = :userId and f.friendStatus <> 'DELETE'")
    public List<Friend> findByToUserId(Long userId);
    public boolean existsByFromUserIdAndToUserId(Long userId,Long toUserId);
}
