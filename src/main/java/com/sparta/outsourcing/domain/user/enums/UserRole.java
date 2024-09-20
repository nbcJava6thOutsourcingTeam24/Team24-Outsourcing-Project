package com.sparta.outsourcing.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    OWNER, //관리자 권한
    USER //사용자 권한
}
