package com.sparta.outsourcing.domain.user.controller;

import com.sparta.outsourcing.domain.user.dto.AuthUser;

import com.sparta.outsourcing.domain.user.dto.request.SignInRequestDto;
import com.sparta.outsourcing.domain.user.dto.request.SignUpRequestDto;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private AuthUser authUser;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 회원_가입_성공() throws Exception{
        String email = "test@test.com";
        String password = "password";
        UserRole userRole = UserRole.USER;
        String bearerToken = "bearerToken";

        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(email, password, userRole);

        given(userService.signUp(email, password, userRole)).willReturn(bearerToken);

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"userRole\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(content().string("회원 가입이 정상적으로 완료되었습니다."));
    }

    @Test
    void 회원_로그인_성공() throws Exception{
        String email = "test@test.com";
        String password = "password";
        String bearerToken = "bearerToken";

        SignInRequestDto signInRequestDto = new SignInRequestDto(email, password);

        given(userService.signIn(email, password)).willReturn(bearerToken);

        mockMvc.perform(post("/api/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(content().string("로그인이 정상적으로 완료되었습니다."));
    }

    @Test
    void 회원_탈퇴_성공() throws Exception{
        Long userId = 1L;
        String password = "password";
        String bearerToken = "bearerToken";

        doNothing().when(userService).delete(userId, password);

        mockMvc.perform(delete("/api/users")
                        .content("{\"password\":\"password\"}")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴가 정상적으로 완료되었습니다"));
    }

}