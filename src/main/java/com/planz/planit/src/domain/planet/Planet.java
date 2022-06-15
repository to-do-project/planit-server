package com.planz.planit.src.domain.planet;

import com.planz.planit.src.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Planet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planet_id")
    private Long planetId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer exp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanetColor color;

    @Column(name="tmp_exp",nullable = false)
    private Integer tmpExp = 0;

    //임시 경험치 올리는 메소드
    public void addTmpExp(){
        this.tmpExp +=1000;
    }
}
