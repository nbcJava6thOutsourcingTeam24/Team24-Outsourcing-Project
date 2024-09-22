package com.sparta.outsourcing.domain.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

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
    void createStore_fail_userUnAuthorization() {
        //given
        User user = new User("user@example.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

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
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

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
        verify(storeRepository, times(1)).findAllByOwnerIdAndStatusFalse(user.getId());
    }


    @Test
    @DisplayName("가게 수정 테스트 - 성공")
    void updateStoere_sucess() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        StoreRequestDto storeUpdateRequestDto = new StoreRequestDto(
            "수정이름",
            LocalTime.parse("13:00"),
            LocalTime.parse("19:00"),
            10000,
            "수정된 공지입니다"
        );

        Store store = new Store(storeRequestDto, user);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        store.update(storeUpdateRequestDto);

        // when
        storeService.updateStore(authUser, 1L, storeUpdateRequestDto);

        // then
        assertEquals("수정이름", store.getName());
        assertEquals(LocalTime.parse("13:00"), store.getOpenTime());
        assertEquals(LocalTime.parse("19:00"), store.getCloseTime());
        assertEquals(10000, store.getMinPrice());
        assertEquals("수정된 공지입니다", store.getNotice());
    }

    @Test
    @DisplayName("가게 수정 테스트 - 실패 - 폐업상태인 가게 수정")
    void updateStore_fail_closedStore() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        Store store = new Store(storeRequestDto, user);
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "status", true);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.updateStore(authUser, 1L, storeRequestDto)
        );

        assertEquals("가게가 존재하지 않습니다", exception.getMessage());
    }

    @Test
    @DisplayName("가게 수정 테스트 - 실패 - user가 다를 경우.")
    void updateStore_fail_user() {
        // given
        User user1 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user1, "id", 1L);

        AuthUser authUser2 = new AuthUser(user2.getId(), user2.getEmail(), user2.getUserRole());

        StoreRequestDto storeRequestDto = new StoreRequestDto(
            "가게이름",
            LocalTime.parse("12:00"),
            LocalTime.parse("18:00"),
            18000,
            "공지입니다"
        );

        Store store = new Store(storeRequestDto, user1);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.updateStore(authUser2, 1L, storeRequestDto)
        );

        assertEquals("계정의 권한이 없습니다.", exception.getMessage());
    }
}
