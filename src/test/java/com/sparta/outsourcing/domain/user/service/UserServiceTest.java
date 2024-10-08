package com.sparta.outsourcing.domain.user.service;

import com.sparta.outsourcing.domain.user.config.auth.JwtUtil;
import com.sparta.outsourcing.domain.user.config.password.PasswordEncoder;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @InjectMocks
    UserService userService;

    @Test
    public void 회원_가입_성공(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);

        //공통
        given(userRepository.findByEmailAndDeletedFalse(email)).willReturn(Optional.empty());
        given(userRepository.findByEmailAndDeletedTrue(email)).willReturn(Optional.empty());
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);

        // 사용자 권한이 USER 일때
        UserRole userRoleUser = UserRole.USER;
        User user = new User(email, encodedPassword, userRoleUser);
        given(jwtUtil.createToken(user.getId(), userRoleUser)).willReturn("token");
        given(userRepository.save(any(User.class))).willReturn(user);

        //when
        String userSignup = userService.signUp(email, password, userRoleUser);

        //then
        assertEquals("token", userSignup);


        // 사용자 권한이 OWNER 일때
        UserRole userRoleOwner = UserRole.OWNER;
        User Owner = new User(email, encodedPassword, userRoleOwner);
        given(jwtUtil.createToken(user.getId(), userRoleOwner)).willReturn("token");
        given(userRepository.save(any(User.class))).willReturn(Owner);

        //when
        String OwnerSignup = userService.signUp(email, password, userRoleOwner);

        //then
        assertEquals("token", OwnerSignup);
    }

    @Test
    void 이미_가입한_사용자(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        UserRole userRoleUser = UserRole.USER;
        User user = new User(email, encodedPassword, userRoleUser);

        given(userRepository.findByEmailAndDeletedFalse(email)).willReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(email, password, userRoleUser));
    }

    @Test
    void 이미_탈퇴한_사용자(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        UserRole userRoleUser = UserRole.USER;
        User user = new User(email, encodedPassword, userRoleUser);

        given(userRepository.findByEmailAndDeletedTrue(email)).willReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(email, password, userRoleUser));
    }


    @Test
    void 회원_로그인_성공(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 권한 생성
        User user = new User(email, encodedPassword, UserRole.USER);
        User owner = new User(email, encodedPassword, UserRole.OWNER);

        //공통
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        // USER 권한
        given(userRepository.findByEmailOrElseThrow(email)).willReturn(user);
        given(jwtUtil.createToken(user.getId(), user.getUserRole())).willReturn("token");

        //when
        String userSignIn = userService.signIn(email, password);

        //then
        assertEquals("token", userSignIn);

        // OWNER 권한
        given(userRepository.findByEmailOrElseThrow(email)).willReturn(owner);
        given(jwtUtil.createToken(owner.getId(), owner.getUserRole())).willReturn("token");

        //when
        String ownerSignIn = userService.signIn(email, password);

        //then
        assertEquals("token", ownerSignIn);
    }

    @Test
    void 로그인_할수없음(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, UserRole.USER);
        user.deleted();

        given(userRepository.findByEmailOrElseThrow(email)).willReturn(user);

        assertThrows(IllegalArgumentException.class, () -> userService.signIn(email, password));
    }

    @Test
    void 비밀번호_불일치(){
        String email = "test@test.com";
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, UserRole.USER);

        given(userRepository.findByEmailOrElseThrow(email)).willReturn(user);
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.signIn(email, password));
    }

    @Test
    void 회원_탈퇴_성공(){
        Long userId = 1L;
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User("test@test.com", encodedPassword, UserRole.USER);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        //when
        userService.delete(userId, password);

        //then
        assertTrue(user.isDeleted());
    }

    @Test
    void 이미탈퇴한_사용자(){
        Long userId = 1L;
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User("test@test.com", encodedPassword, UserRole.USER);

        user.deleted();

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);

        assertThrows(IllegalArgumentException.class, () -> userService.delete(userId, password));
    }

    @Test
    void 비밀번호_일치하지_않음(){
        Long userId = 1L;
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User("test@test.com", encodedPassword, UserRole.USER);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.delete(userId, password));
    }

}