package com.planz.planit.src.domain.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 값 타입
@Embeddable
public class Position {

    @Column(name = "pos_x")
    private Float posX;

    @Column(name = "pos_y")
    private Float posY;

    // 값타입인 경우 equals()와 hashCode() 함수 필수 !!!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;

        // 프록시일 경우, 필드에 직접 접근할 수 없다.
        // 따라서 getter()를 이용해서 필드에 접근해야 한다.
        return Objects.equals(getPosX(), position.getPosX()) && Objects.equals(getPosY(), position.getPosY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosX(), getPosY());
    }
}
