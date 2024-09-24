package com.sparta.outsourcing.domain.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.dto.response.StoreResponseDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.service.StoreService;
import com.sparta.outsourcing.domain.user.config.auth.AuthUserArgumentResolver;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.GlobalExceptionHandler;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;


@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    private StoreController storeController;

    @MockBean
    private StoreService storeService;

    @Mock
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storeController, authUserArgumentResolver)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(authUserArgumentResolver)
            .addFilters(new CharacterEncodingFilter("UTF-8", true))
            .build();
    }


    @Test
    @DisplayName("가게 조회 - 성공")
    void getStore() throws Exception {
        // when
        this.mockMvc
            .perform(get("/api/stores/1"))
            .andExpect(status().isOk())
            .andDo(print());

        //then
        verify(storeService).getStore(any(Long.class));
    }

    @Test
    @DisplayName("가게 생성 - 성공")
    void createStore() throws Exception {
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.OWNER);

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        doNothing().when(storeService).createStore(authUser, storeRequestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/stores")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(storeRequestDto))
        );

        // then
        resultActions.andExpect(status().isCreated());
//        resultActions.andExpect(content().string("가게 생성이 완료되었습니다"));
    }

    @Test
    @DisplayName("가게 조회 - 성공")
    void getStoreList() throws Exception {
        // given
        String name = "가게이름";

        StoreResponseDto storeResponseDto = new StoreResponseDto(
            1L,
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        StoreResponseDto storeResponseDto2 = new StoreResponseDto(
            2L,
            "가게이름2",
            LocalTime.parse("13:00"),
            LocalTime.parse("19:00"),
            22000,
            "하이하이"
        );

        // when
        when(storeService.getStoreList(name)).thenReturn(List.of(storeResponseDto, storeResponseDto2));

        // then
        mockMvc.perform(get("/api/stores?name={name}", name))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(storeResponseDto.getId()))
            .andExpect(jsonPath("$[1].id").value(storeResponseDto2.getId()))
            .andExpect(jsonPath("$[0].name").value(storeResponseDto.getName()))
            .andExpect(jsonPath("$[1].name").value(storeResponseDto2.getName()))
//            .andExpect(jsonPath("$[0].openTime").value("12:00"))
//            .andExpect(jsonPath("$[0].closeTime").value("18:00"))
            .andExpect(jsonPath("$[0].minPrice").value(18000))
            .andExpect(jsonPath("$[0].notice").value("공지입니다"))
//            .andExpect(jsonPath("$[1].openTime").value("13:00"))
//            .andExpect(jsonPath("$[1].closeTime").value("19:00"))
            .andExpect(jsonPath("$[1].minPrice").value(22000))
            .andExpect(jsonPath("$[1].notice").value("하이하이"));
    }

    @Test
    @DisplayName("가게 단건 조회 - 성공")
    void getStoreById() throws Exception {
        // given
        long storeId = 1L;

        StoreResponseDto storeResponseDto = new StoreResponseDto(
            1L,
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        // when
        when(storeService.getStore(storeId)).thenReturn(storeResponseDto);

        // then
        mockMvc.perform(get("/api/stores/{storeId}", storeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(storeId))
            .andExpect(jsonPath("$.name").value("가게이름"))
//            .andExpect(jsonPath("$.openTime").value("12:00"))
//            .andExpect(jsonPath("$.closeTime").value("18:00"));
            .andExpect(jsonPath("$.minPrice").value(18000))
            .andExpect(jsonPath("$.notice").value("공지입니다"));
    }

    @Test
    @DisplayName("가게 수정")
    void updateStore() throws Exception {
        // given
        long storeId = 1L;
        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지"
            );

        User user = new User("email@example.com", "1234", UserRole.OWNER);

        Store store = new Store(storeRequestDto, user);
        ReflectionTestUtils.setField(store, "id", 1L);

        StoreRequestDto updatedStoreRequestDto = new StoreRequestDto(
            "변경된 가게이름",
            LocalTime.parse("13:00"),
            LocalTime.parse("19:00"),
            22000,
            "공지공지"
        );

        StoreResponseDto storeResponseDto = new StoreResponseDto(
            1L,
            "변경된 가게이름",
            LocalTime.parse("13:00"),
            LocalTime.parse("19:00"),
            22000,
            "공지공지"
        );

        store.update(updatedStoreRequestDto);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/stores/{storeId}", storeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(storeRequestDto))
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 삭제")
    void deleteStore() throws Exception {
        // given
        long storeId = 1L;
        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지"
        );

        User user = new User("email@example.com", "1234", UserRole.OWNER);

        Store store = new Store(storeRequestDto, user);
        ReflectionTestUtils.setField(store, "id", storeId);

        store.delete();

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/stores/{storeId}", storeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(storeRequestDto))
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 광고")
    void enableAdvertisement() throws Exception {
        // given
        this.mockMvc
            .perform(post("/api/stores/1/advertisement"))
            .andExpect(status().isCreated())
            .andDo(print());
    }
}
