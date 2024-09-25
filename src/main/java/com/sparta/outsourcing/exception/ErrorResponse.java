package com.sparta.outsourcing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final HttpStatus status;
    private final String message;

    private ErrorResponse(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    public static ErrorResponse of(final HttpStatus status, final String message) {
        return new ErrorResponse(status, message);
    }

    // 확장성을 고려하여 주석 처리 추후, 상태 코드(int)까지 필요하여 반환하고 싶다면 아래 메서드를 추가
    /*
    public int getStatusCode() {
        return status.value(); // HttpStatus에서 int로 변환된 코드
    }
    */

}