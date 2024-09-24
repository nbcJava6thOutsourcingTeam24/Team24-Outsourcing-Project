package com.sparta.outsourcing.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeleteDto {

    @NotBlank
    private String password;
}
