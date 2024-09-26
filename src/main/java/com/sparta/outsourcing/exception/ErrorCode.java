package com.sparta.outsourcing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_STORE_SIZE(HttpStatus.BAD_REQUEST, "가게는 최대 3개만 등록 가능합니다."),

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_LOGIN_INPUT(HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호가 일치하지 않습니다."),
    ALREADY_USER_EXIST(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    PASSWORD_SAME_OLD(HttpStatus.CONFLICT, "기존 비밀번호와 동일합니다."),
    USER_FORBIDDEN(HttpStatus.FORBIDDEN, "계정의 권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "고객이 존재하지 않습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게가 존재하지 않습니다."),

    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴가 존재하지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    MINIMUM_ORDER_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다."),
    STORE_CLOSED(HttpStatus.FORBIDDEN, "가게가 영업중이 아닙니다."),

    INVALID_USER_FOR_ORDER(HttpStatus.FORBIDDEN, "주문 취소 권한이 없습니다. 해당 주문을 생성한 유저만 주문을 취소할 수 있습니다."),
    ORDER_STATUS_CHANGE_FORBIDDEN(HttpStatus.FORBIDDEN, "주문 상태 변경 권한이 없습니다. 사장님만 주문 상태를 변경할 수 있습니다."),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "비정상적인 주문 상태 변경 요청입니다."),
    INVALID_TRANSITION_FROM_ORDER_CONFIRMED_TO_CANCELLED(HttpStatus.BAD_REQUEST, "주문 확인 상태에서만 주문 취소가 가능합니다."),
    INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED(HttpStatus.BAD_REQUEST, "주문 준비 중 상태에서만 주문 취소가 가능합니다."),
    INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED(HttpStatus.BAD_REQUEST, "배달 완료 상태에서는 주문 취소가 불가능합니다."),
    ALREADY_ORDER_STATUS(HttpStatus.BAD_REQUEST, "이미 해당 주문 상태입니다."),
    INVALID_OWNER_FOR_ORDER(HttpStatus.FORBIDDEN, "주문 상태를 변경할 권한이 없습니다. 해당 가게의 사장님만 주문 상태를 변경할 수있습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "주문 요청 상태가 올바르지 않습니다."),
    INVALID_ORDER_CREATION_FOR_OWNER(HttpStatus.FORBIDDEN, "사장님은 주문을 요청할 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "주문 접근 권한이 없습니다."),

    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 주문에 이미 리뷰가 존재합니다."),
    INVALID_ROLE_FOR_REVIEW_CREATION(HttpStatus.FORBIDDEN, "리뷰 작성 권한이 없습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "배달 완료된 주문에만 리뷰를 작성할 수 있습니다."),
    INVALID_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "리뷰에 접근할 권한이 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰가 존재하지 않습니다."),

    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"만료된 JWT 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED,"지원되지 않는 JWT 토큰입니다."),
    EMPTY_CLAIMS(HttpStatus.BAD_REQUEST,"잘못된 JWT 토큰입니다."),
    TOKEN_VERIFICATION_ERROR(HttpStatus.UNAUTHORIZED,"JWT 토큰 검증 중 오류가 발생했습니다."),
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND,"JWT 토큰을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(final HttpStatus httpStatus, final String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
