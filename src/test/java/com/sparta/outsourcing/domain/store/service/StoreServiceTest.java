package com.sparta.outsourcing.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("가게 생성 테스트 - 성공")
    void createStore_success() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.OWNER);
        User user = new User(authUser.getEmail(), "1234", authUser.getUserRole());
        ReflectionTestUtils.setField(user, "id", 1L);

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(storeRepository.findAllByOwnerIdAndStatusFalse(user.getId())).thenReturn(Collections.emptyList());

        // when
        storeService.createStore(authUser, storeRequestDto);

        // then
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("가게 생성 테스트 - 실패 - 유저 권한 실패")
    void createStore_fail_() {
        //given
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.USER);
        User user = new User(authUser.getEmail(), "1234", authUser.getUserRole());
        ReflectionTestUtils.setField(user, "id", 1L);

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.createStore(authUser, storeRequestDto)
        );

        assertEquals("계정의 권한이 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("가게 생성 테스트 - 실패 - 가게가 3개 이상 있을때.")
    void createStore_fail_store_limit() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.OWNER);
        User user = new User(authUser.getEmail(), "1234", authUser.getUserRole());
        ReflectionTestUtils.setField(user, "id", 1L);

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(storeRepository.findAllByOwnerIdAndStatusFalse(user.getId())).thenReturn(Collections.nCopies(3, new Store()));

        // when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.createStore(authUser, storeRequestDto)
        );

        assertEquals("가게는 최대 3개만 등록 가능합니다", exception.getMessage());
    }
}
