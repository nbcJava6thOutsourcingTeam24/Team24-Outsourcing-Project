package com.sparta.outsourcing.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequestDto {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;

}
