package com.sparta.outsourcing.domain.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.outsourcing.domain.menu.dto.request.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.response.CreateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.service.MenuService;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.user.config.auth.AuthUserArgumentResolver;
import com.sparta.outsourcing.domain.user.config.auth.JwtFilter;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mvc;

//    @Autowired
//    private WebApplicationContext context;

    @MockBean
    private JwtFilter jwtFilter;

    @Autowired
    private MenuController menuController;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(menuController, authUserArgumentResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authUserArgumentResolver)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }
    @Test
    void createMenuControllerTest() throws Exception {
        // given
        Long storeId = 1L;
        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("짬뽕", 7000L);

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.OWNER);

        // Argument Resolver가 AuthUser 객체를 반환하도록 설정
        when(authUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(authUser);

        Store store = new Store();
        Menu menu = new Menu(createMenuRequestDto, store);
        CreateMenuResponseDto createMenuResponseDto = new CreateMenuResponseDto(menu);

        when(menuService.createMenu(eq(storeId), any(CreateMenuRequestDto.class),
                any(AuthUser.class))).thenReturn(createMenuResponseDto);

        // CreateMenuRequestDto를 JSON으로 변환
        String requestBody = objectMapper.writeValueAsString(createMenuRequestDto);

        // when & then
        mvc.perform(post("/api/{storeId}/menus", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody) // JSON 바디 추가
                        .header("Authorization", "Bearer valid-token")) // JWT 헤더 추가
                .andExpect(status().isCreated()); // 201 CREATED 상태 코드 검증
//            .andExpect((ResultMatcher) content().string("메뉴 생성이 완료되었습니다.")); // 응답 본문 검증

        // 메뉴 생성 서비스가 호출되었는지 확인
        verify(menuService, times(1)).createMenu(eq(storeId), any(CreateMenuRequestDto.class), any(AuthUser.class));
    }
}