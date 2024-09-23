//package com.sparta.outsourcing.user;
//
//import com.sparta.outsourcing.domain.user.controller.UserController;
//import com.sparta.outsourcing.domain.user.dto.request.SignUpRequestDto;
//import com.sparta.outsourcing.domain.user.enums.UserRole;
//import com.sparta.outsourcing.domain.user.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//
//
//
//@WebMvcTest(UserController.class)
//public class UserControllerTest {
//
//    @MockBean
//    private UserService userService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void 회원_가입_성공(){
//        String email = "test@test.com";
//        String password = "password";
//        UserRole userRole = UserRole.USER;
//        String bearerToken = "bearerToken";
//
//        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(email, password, userRole);
//
//        given(userService.signUp(email, password, userRole)).willReturn(bearerToken);
//
////        mockMvc.perform(post("/api/signup")
////                .contentType(MediaType.APPLICATION_JSON)
////                .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"userRole\":\"User\"}"))
////                .andExpect(status().isCreated())
////                .andExpect(header().string(HttpHeaders.AUTHORIZATION, bearerToken))
////                .andExpect(content().string("회원 가입이 정상적으로 완료되었습니다."));
////    }
//
//}
