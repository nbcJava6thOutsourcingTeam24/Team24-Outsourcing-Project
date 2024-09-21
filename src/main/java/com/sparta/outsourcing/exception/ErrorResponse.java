package com.sparta.outsourcing.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final int statusCode;
    private final String message;
    private final Map<String, String> validations = new HashMap<>();

    private ErrorResponse(final int statusCode, final String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public static ErrorResponse of(final int statusCode, final String message) {
        return new ErrorResponse(statusCode, message);
    }

    public void addValidation(final String fieldName, final String message) {
        validations.put(fieldName, message);
    }
}
