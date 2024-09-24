package com.sparta.outsourcing.domain.user.dto.request;


import com.sparta.outsourcing.domain.user.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {

    @NotBlank
    @Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "이메일 형식은~@~.~를 지켜주세요.")
    private String email;

    @NotBlank
    private String password;

    private UserRole userRole;

}
