package com.sparta.outsourcing.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request - 유효성 검증 실패 시 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "잘못된 요청입니다.";  //  <<< 커스텀 메시지

        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        // 필드 에러 출력 및 에러 응답에 추가
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            log.error("유효성 검증 실패 - 필드: {}, 오류: {}", fieldError.getField(), fieldError.getDefaultMessage());
            errorResponse.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return errorResponse;
    }

    // 409 Conflict - 데이터베이스 무결성 위반 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("데이터 무결성 위반 - {}", e.getMessage());

        HttpStatus status = HttpStatus.CONFLICT;
        String message = "데이터베이스 무결성 위반: 중복된 값 또는 외래 키 제약을 위반했습니다.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);
        return ResponseEntity.status(status).body(errorResponse);
    }


    // 400 Bad Request - 제약 조건 위반 ( 제약 조건 위반이라는게 모호한 같아 추가로 설명하면 예시)필드 값 검증 실패)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException e) {
        log.error("제약 조건 위반 - {}", e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "입력된 값이 제약 조건을 위반했습니다. 데이터를 확인해주세요.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        // 구체적인 제약 조건 위반 메시지 추가
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errorResponse.addValidation(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    // 400 Bad Request - 잘못된 주문 상태 값 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        log.error("잘못된 요청 타입 - {}", e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "잘못된 주문 상태 값입니다. 요청된 값이 올바르지 않습니다.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        return ResponseEntity.status(status).body(errorResponse);
    }

    // 400 Bad Request - 누락된 주문 요청 파라미터 처리 (URL 형식 오류)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("필수 요청 파라미터 누락 - {} : {}", e.getParameterName(), e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다. URL 형식을 확인해주세요.", e.getParameterName());
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        return ResponseEntity.status(status).body(errorResponse);
    }

    // 400 Bad Request - 잘못된 JSON 형식 또는 누락된 값 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.error("잘못된 요청 본문 - {}", e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "잘못된 JSON 형식이거나 누락된 값이 있습니다. 요청 데이터를 확인해주세요.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        return ResponseEntity.status(status).body(errorResponse);
    }

    // 409 Conflict - SQL 제약 조건 위반
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolationException(final SQLIntegrityConstraintViolationException e) {
        log.error("SQL 제약 조건 위반 - {}", e.getMessage());

        HttpStatus status = HttpStatus.CONFLICT;
        String message = "데이터베이스 제약 조건 위반: 중복된 값 또는 외래 키 제약을 위반했습니다.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        return ResponseEntity.status(status).body(errorResponse);
    }


    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(final ApplicationException e) {
        log.error("비즈니스 예외 발생 - {} : {}", e.getHttpStatus(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    // 500 Internal Server Error - 처리되지 않은 예외 (예상치 못한 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(final Exception e) {
        log.error("예상치 못한 예외 발생 - {}", e.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "서버에서 처리할 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.";
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        return ResponseEntity.status(status).body(errorResponse);
    }


}
