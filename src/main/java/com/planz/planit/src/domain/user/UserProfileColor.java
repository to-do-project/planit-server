package com.planz.planit.src.domain.user;

import com.planz.planit.config.BaseResponseStatus;

public enum UserProfileColor {
    LightRed,
    Yellow,
    Green,
    SkyBlue,
    Blue,
    LightPurple,
    Purple,
    Pink,
    Gray,
    Black;

    public static UserProfileColor of(final String profileColor){
        // valueOf : 이름을 가지고 객체로 가져오는 함수
        return UserProfileColor.valueOf(profileColor);
    }
}
