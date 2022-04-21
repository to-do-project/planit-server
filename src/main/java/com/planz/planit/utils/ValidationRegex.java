package com.planz.planit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationRegex {
    // 이메일 형식 체크
    public static boolean isRegexEmail(String target) {
        if (target.length() > 50){
            return false;
        }

        String regex = "^[A-Za-z0-9._^%$~#+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
//        String regex = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    public static boolean isRegexPassword(String target){
        // 영어 + 숫자 6~15
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,15}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    // 날짜 형식, 전화 번호 형식 등 여러 Regex 인터넷에 검색하면 나옴.
}

