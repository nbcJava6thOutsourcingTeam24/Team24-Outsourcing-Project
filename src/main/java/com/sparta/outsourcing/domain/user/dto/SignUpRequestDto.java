package com.sparta.outsourcing.domain.user.dto;


import com.sparta.outsourcing.domain.user.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignUpRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private UserRole userRole;

}
