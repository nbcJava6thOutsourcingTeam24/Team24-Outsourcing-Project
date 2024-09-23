package com.sparta.outsourcing.domain.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.dto.response.StoreResponseDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Mock
    private MenuRepository menuRepository;

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

        when(storeRepository.findAllByOwnerIdAndStatusFalse(user.getId())).thenReturn(
            Collections.emptyList());

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

        when(storeRepository.findAllByOwnerIdAndStatusFalse(user.getId())).thenReturn(
            Collections.nCopies(3, new Store()));

        // when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.createStore(authUser, storeRequestDto)
        );

        assertEquals("가게는 최대 3개만 등록 가능합니다.", exception.getMessage());
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

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
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

    @Test
    @DisplayName("가게 목록 조회 테스트 - 성공")
    void getStoreList_success() {
        // given
        String storeName = "가게";
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Store store1 = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지1"), user);
        ReflectionTestUtils.setField(store1, "id", 1L);

        Store store2 = new Store(
            new StoreRequestDto("가게", LocalTime.parse("13:00"), LocalTime.parse("20:00"), 20000,
                "공지2"), user);
        ReflectionTestUtils.setField(store2, "id", 2L);

        List<Store> stores = Arrays.asList(store1, store2);

        when(storeRepository.findStoreByName(storeName)).thenReturn(stores);

        // when
        List<StoreResponseDto> storeResponseDtos = storeService.getStoreList(storeName);

        // then
        assertEquals(2, storeResponseDtos.size());
        assertEquals("가게", storeResponseDtos.get(0).getName());
        assertEquals("가게", storeResponseDtos.get(1).getName());
    }

    @Test
    @DisplayName("가게 목록 조회 테스트 - 실패 - 스토어가 존재하지 않을때")
    void getStoreList_fail_notFoundStore() {
        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.getStoreList("가게")
        );

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("가게 단건 조회 테스트 - 성공")
    void getStore_success() {
        // given
        User user = new User("user@exampel.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        StoreResponseDto storeResponseDto = storeService.getStore(store.getId());

        // then
        assertEquals("가게", storeResponseDto.getName());
        assertEquals(LocalTime.parse("12:00"), storeResponseDto.getOpenTime());
        assertEquals(LocalTime.parse("18:00"), storeResponseDto.getCloseTime());
        assertEquals(18000, storeResponseDto.getMinPrice());
        assertEquals("공지", storeResponseDto.getNotice());
    }

    @Test
    @DisplayName("가게 단건 조회 테스트 - 실패 - 가게폐쇄")
    void getStore_fail_storeIsClosed() {
        // given
        User user = new User("user@exampel.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "status", true);

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.getStore(1L)
        );

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("\"가게 단건 조회 테스트 - 실패 - 가게가 없을때")
    void getStore_fail_storeNotFound() {
        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.getStore(1L)
        );

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("가게 삭제 테스트 - 성공")
    void deleteStore_success() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById((user.getId()))).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        storeService.deleteStore(authUser, store.getId());

        // then
        assertEquals(true, store.isStatus());
    }

    @Test
    @DisplayName("가게 삭제 테스트 - 실패 - 이미 영업종료된 가게")
    void deleteStore_fail_notFoundStore() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "status", true);

        when(userRepository.findById((user.getId()))).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.deleteStore(authUser, store.getId())
        );

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("가게 삭제 테스트 - 실패 - 다른 유저가 삭제하려고 시도할때")
    void deleteStore_fail_anotherUser() {
        // given
        User user1 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user2, "id", 2L);

        AuthUser authUser2 = new AuthUser(user2.getId(), user2.getEmail(), user2.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user1);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById((user2.getId()))).thenReturn(Optional.of(user2));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.deleteStore(authUser2, store.getId())
        );

        assertEquals("계정의 권한이 없습니다.", exception.getMessage());
    }


    @Test
    @DisplayName("가게 광고 테스트 - 성공")
    void addAdvertisement_success() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById((user.getId()))).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        // when
        storeService.createAdvertisement(authUser, store.getId());

        // then
        assertEquals(true, store.isAdvertised());
    }

    @Test
    @DisplayName("가게 광고 테스트 - 실패 - 이미 영업종료된 가게")
    void addAdvertisement_fail_notFoundStore() {
        // given
        User user = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user);
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "status", true);

        when(userRepository.findById((user.getId()))).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.createAdvertisement(authUser, store.getId())
        );

        assertEquals("가게가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("가게 광고 테스트 - 실패 - 다른 유저가 삭제하려고 시도할때")
    void addAdvertisement_fail_anotherUser() {
        // given
        User user1 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = new User("user@example.com", "1234", UserRole.OWNER);
        ReflectionTestUtils.setField(user2, "id", 2L);

        AuthUser authUser2 = new AuthUser(user2.getId(), user2.getEmail(), user2.getUserRole());

        Store store = new Store(
            new StoreRequestDto("가게", LocalTime.parse("12:00"), LocalTime.parse("18:00"), 18000,
                "공지"), user1);
        ReflectionTestUtils.setField(store, "id", 1L);

        when(userRepository.findById((user2.getId()))).thenReturn(Optional.of(user2));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        //when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            storeService.createAdvertisement(authUser2, store.getId())
        );

        assertEquals("계정의 권한이 없습니다.", exception.getMessage());
    }
}
