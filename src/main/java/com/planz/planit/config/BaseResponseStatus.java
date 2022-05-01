package com.planz.planit.config;
import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"중복된 이메일입니다."),



    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false,3014,"없는 아이디거나 비밀번호가 틀렸습니다."),



    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userId}
    MODIFY_FAIL_USERNAME(false,4014,"유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다."),


    // 5000 : 은지
    NOT_EXIST_FLAG_ENUM(false,5000,"존재하지 않는 알림 설정입니다."),
    // 6000 : 필요시 만들어서 쓰세요

    INVALID_ACCESS_TOKEN(false, 6000, "만료된 access token 입니다."),
    NOT_EXIST_USER(false, 6001, "존재하지 않는 사용자입니다."),
    INVALID_ID_OR_PWD(false, 6002, "이메일 혹은 비밀번호를 잘못 입력했습니다."),
    IO_EXCEPTION(false, 6003, "로그인 시도 중에 IOException이 발생했습니다."),
    NOT_EXIST_ACCESS_TOKEN_SUBJECT(false, 6004, "access token에 subject가 존재하지 않습니다."),
    INVALID_USER_ID(false, 6005, "유효하지 않은 userId입니다. 숫자형태로 입력해주세요."),
    NOT_EQUAL_USER_ID(false, 6006, "accessToken 내의 userId와 header 내의 userId가 일치하지 않습니다."),
    NOT_EXIST_USER_ID_IN_HEADER(false, 6007, "userId를 헤더에 입력해주세요"),
    NOT_EXIST_ACCESS_TOKEN_IN_HEADER(false, 6008, "access token을 헤더에 입력해주세요."),
    NOT_EXIST_LOGIN_REQ_DTO(false, 6009, "email, password, deviceToken을 모두 입력해주세요."),
    INVALID_EMAIL_FORM(false, 6010, "이메일 형식이 올바르지 않습니다. (50자 이내)"),
    INVALID_PWD_FORM(false, 6011, "비밀번호 형식이 올바르지 않습니다. (영문+숫자 6~15자)"),
    NOT_EXIST_JOIN_REQ_DTO(false, 6012, "email, password, nickname, planetColor, deviceToken을 모두 입력해주세요."),
    INVALID_NICKNAME_FORM(false, 6013, "닉네임 형식이 올바르지 않습니다. (영문+한글+숫자 20자 이내)"),
    INVALID_PLANET_COLOR_FORM(false, 6014, "행성 색깔이 올바르지 않습니다. (RED, GREEN, BLUE 중 하나)"),
    ALREADY_EXIST_EMAIL(false, 6015, "이미 존재하는 이메일입니다."),
    ALREADY_EXIST_NICKNAME(false, 6016, "이미 존재하는 닉네임입니다."),
    NOT_EXIST_EMAIL(false, 6017, "email을 입력해주세요."),
    MAIL_SEND_ERROR(false, 6018, "메일을 전송하는데 실패했습니다."),
    NOT_EXIST_AUTH_NUM_IN_REDIS(false, 6019, "발급받은 인증번호가 없습니다."),
    INVALID_AUTH_NUM(false, 6020, "인증번호가 일치하지 않습니다."),
    NOT_EXIST_AUTH_NUM_IN_BODY(false, 6021, "인증번호를 입력해주세요."),
    INVALID_AUTH_NUM_FORM(false, 6022, "인증번호 형식이 올바르지 않습니다. (6자리 숫자)"),
    INVALID_REFRESH_TOKEN(false, 6023, "유효하지 않은 refresh token 입니다. 다시 로그인해주세요."),
    NOT_EXIST_REFRESH_TOKEN_IN_HEADER(false, 6024, "'Bearer '로 시작하는 refresh token을 헤더에 입력해주세요."),
    FAIL_WITHDRAWAL(false, 6025, "User 테이블에서 해당 정보를 삭제하는데 실패했습니다."),
    INVALID_PASSWORD(false, 6026, "비밀번호가 일치하지 않습니다."),
    NOT_EXIST_PASSWORD(false, 6027, "비밀번호를 입력해주세요."),
    TWO_REFRESH_TOKEN_NOT_EQUAL(false, 6028, "헤더의 refresh token과 서버에 저장된 refresh token이 일치하지 않습니다."),
    NOT_EXIST_OLD_PASSWORD(false, 6029, "기존 비밀번호를 입력해주세요."),
    NOT_EXIST_NEW_PASSWORD(false, 6030, "새로운 비밀번호를 입력해주세요."),
    NOT_EXIST_NICKNAME(false, 6031, "새로운 닉네임을 입력해주세요.")
    ;


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

    public static BaseResponseStatus of(final String errorName){
        // valueOf : 이름을 가지고 객체로 가져오는 함수
        return BaseResponseStatus.valueOf(errorName);
    }
}
