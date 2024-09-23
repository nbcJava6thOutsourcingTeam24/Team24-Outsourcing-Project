package com.sparta.outsourcing.exception;

import org.hibernate.sql.Delete;

public enum ErrorCode {

    BAD_REQUEST(400, "잘못된 요청입니다."),
    INVALID_STORE_SIZE(400, "가게는 최대 3개만 등록 가능합니다"),

    TOKEN_INVALID(401, "토큰이 유효하지 않습니다."),

    USER_FORBIDDEN(403, "계정의 권한이 없습니다."),

    PASSWORD_NOT_MATCH(404, "비밀번호가 일치하지 않습니다"),
    USER_NOT_FOUND(404, "유저가 존재하지 않습니다"),
    STORE_NOT_FOUND(404, "가게가 존재하지 않습니다"),

    ALREADY_USER_EXIST(409, "이미 존재하는 회원입니다"),
    PASSWORD_SAME_OLD(409, "기존 비밀번호와 동일합니다");

    private final int statusCodee;
    private final String message;

    ErrorCode(final int statusCodee, final String message) {
        this.statusCodee = statusCodee;
        this.message = message;
    }

    public int statusCode() {
        return statusCodee;
    }

    public String message() {
        return message;
    }
}
