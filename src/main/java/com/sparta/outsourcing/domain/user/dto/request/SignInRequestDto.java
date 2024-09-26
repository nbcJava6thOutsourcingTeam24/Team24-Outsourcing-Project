package com.sparta.outsourcing.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInRequestDto {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;

}
