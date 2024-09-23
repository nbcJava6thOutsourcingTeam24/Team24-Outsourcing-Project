package com.sparta.outsourcing.domain.user.controller;

import com.sparta.outsourcing.domain.user.config.annotation.Auth;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.dto.request.SignInRequestDto;
import com.sparta.outsourcing.domain.user.dto.request.SignUpRequestDto;
import com.sparta.outsourcing.domain.user.dto.request.UserDeleteDto;
import com.sparta.outsourcing.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpRequestDto requestDto) {
       String bearerToken = userService.signUp(requestDto.getEmail(), requestDto.getPassword(), requestDto.getUserRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body("회원 가입이 정상적으로 완료되었습니다.");
    }


    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody SignInRequestDto requestDto){
        String bearerToken = userService.signIn(requestDto.getEmail(), requestDto.getPassword());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body("로그인이 정상적으로 완료되었습니다.");
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> delete(@Auth AuthUser authUser, @RequestBody UserDeleteDto request){
        userService.delete(authUser.getId(), request.getPassword());
        return ResponseEntity.ok("회원 탈퇴가 정상적으로 완료되었습니다");
    }
}
