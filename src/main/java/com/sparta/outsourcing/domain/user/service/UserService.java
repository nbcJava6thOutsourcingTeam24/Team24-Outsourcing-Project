package com.sparta.outsourcing.domain.user.service;

import com.sparta.outsourcing.domain.user.config.auth.JwtUtil;
import com.sparta.outsourcing.domain.user.config.password.PasswordEncoder;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signUp(String email, String password, UserRole userRole) {
      if(!userRepository.findByEmailAndDeletedFalse(email).isEmpty()){
          throw new IllegalArgumentException("이미 가입한 사용자 입니다.");
      }
      if(!userRepository.findByEmailAndDeletedTrue(email).isEmpty()){
          throw new IllegalArgumentException("이미 탈퇴한 사용자 입니다.");
      }

        User user = new User(email, passwordEncoder.encode(password), userRole);

        return jwtUtil.createToken(userRepository.save(user).getId(), userRole);
    }

    @Transactional
    public String signIn(String email, String password) {
        User user = userRepository.findByEmailOrElseThrow(email);
        if(user.isDeleted()){
            throw new IllegalArgumentException("이미 탈퇴한 사용자 입니다.");
        }
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.createToken(user.getId(), user.getUserRole());
    }

    @Transactional
    public void delete(Long id, String password) {
        User user = userRepository.findByIdOrElseThrow(id);

        if(user.isDeleted()){
            throw new IllegalArgumentException("이미 탈퇴한 사용자 입니다.");
        }
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.deleted();
        userRepository.save(user);
    }
}
