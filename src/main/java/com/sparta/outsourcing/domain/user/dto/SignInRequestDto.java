package com.sparta.outsourcing.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignInRequestDto {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;

}
