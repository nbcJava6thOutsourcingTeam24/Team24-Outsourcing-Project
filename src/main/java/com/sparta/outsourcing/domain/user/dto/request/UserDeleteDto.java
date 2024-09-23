package com.sparta.outsourcing.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserDeleteDto {

    @NotBlank
    private String password;
}
