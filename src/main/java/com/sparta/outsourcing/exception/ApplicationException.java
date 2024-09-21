package com.sparta.outsourcing.exception;

public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApplicationException(final ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return errorCode.statusCode();
    }
}
