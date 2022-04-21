package com.planz.planit.src.domain.user;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
//    @OneToOne
//    @JoinColumn(name = "planetId")
    private Long planetId;


    @Column(unique = true, length = 50)
    private String email;

    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    private Integer characterColor = 0;

    private Integer profileColor = 0;

    private Integer point = 0;

    private Integer missionStatus = 1;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private LocalDateTime createAt;

    private LocalDateTime lastCheckAt;

/*    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();*/

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @PrePersist
    public void setTime(){
        this.createAt = LocalDateTime.now();
        this.lastCheckAt = LocalDateTime.now();
    }

    @Transient
    private String deviceToken;

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
