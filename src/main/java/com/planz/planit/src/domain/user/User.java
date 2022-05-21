package com.planz.planit.src.domain.user;

import com.planz.planit.src.domain.planet.Planet;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Planet planet;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(name="pwd",nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 8)
    private String nickname;

    @Column(name = "character_item", nullable = false)
    private Long characterItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_color", nullable = false)
    private UserProfileColor profileColor;

    @Column(name="user_point", nullable = false)
    private Integer point = 0;

    @Column(name = "mission_status", nullable = false)
    private Integer missionStatus = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "last_check_at")
    private LocalDateTime lastCheckAt;

/*    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();*/

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @PrePersist
    public void setTime(){
        this.createAt = LocalDateTime.now();
        this.lastCheckAt = LocalDateTime.now();
    }

    // 디바이스 토큰 값
    @Transient
    private String deviceToken;

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public void setCharacterItem(Long characterItem) {
        this.characterItem = characterItem;
    }

    public void setProfileColor(UserProfileColor profileColor) {
        this.profileColor = profileColor;
    }
}
