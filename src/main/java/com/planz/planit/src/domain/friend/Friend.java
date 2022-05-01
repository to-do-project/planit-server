package com.planz.planit.src.domain.friend;

import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.planz.planit.src.domain.friend.FriendStatus.WAIT;


//https://github.com/moonsbeen626/clone_instargram/blob/master/src/main/java/moon/clone/instargram/domain/follow/Follow.java
@Getter
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name="subscribe_uk",
                        columnNames = {"from_user_id", "to_user_id"}
                )
        }
)
public class Friend {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="friend_id")
    private Long friendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="from_user_id",nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="to_user_id",nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name="friend_status",nullable = false)
    private FriendStatus friendStatus=WAIT;

    @Column(name="create_at",nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Builder
    public Friend(User fromUser,User toUser){
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    //친구 수락 및 친구 거절
    public void acceptFriend(boolean accepted){
        if(accepted) {
            this.friendStatus = FriendStatus.FRIEND;
        }else{
            this.friendStatus = FriendStatus.DELETE;
        }
    }
}
