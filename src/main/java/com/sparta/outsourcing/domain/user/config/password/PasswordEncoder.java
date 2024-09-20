package com.sparta.outsourcing.domain.user.config.password;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordEncoder {
    public String encode(String rawPassword) {

        String passwordPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z0-9$@$!%*#?&]{8,}$";

        Pattern pattern = Pattern.compile(passwordPattern);
        if (!pattern.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 대문자와 특수 문자를 포함해야 합니다.");
        }

        return BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}
