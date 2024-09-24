package com.sparta.outsourcing.domain.menu.service;

import com.sparta.outsourcing.domain.menu.dto.request.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.response.CreateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.dto.response.UpdateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    MenuService menuService;

    @Test
    void menu를_정상적으로_등록한다(){
        // menu 생성을 테스트 -> menuSerivce.create가 잘 작동하는지 테스트
        // user
        // store
        //given

        User user = new User("email", "password", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "storeName");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(12,30));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(23,30));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice",10000);
        Store store = new Store(storeRequestDto, user);
        ReflectionTestUtils.setField(store, "id",1L);
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));


        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("짜장면", 4000L);
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(),UserRole.OWNER);

        Menu menu = new Menu(createMenuRequestDto, store);
        ReflectionTestUtils.setField(menu, "id",1L);
        given(menuRepository.save(any(Menu.class))).willReturn(menu);


        //when
        CreateMenuResponseDto createdMenu = menuService.createMenu(store.getId(), createMenuRequestDto, authUser);

        //then
        Assertions.assertThat(createdMenu.getMenuname()).isEqualTo(createMenuRequestDto.getMenuname());
    }

    @Test
    void menu등록_실패_유저권한이_OWNER아닌경우(){
        // given
        User currentUser = new User("user@example.com", "password", UserRole.USER); // USER 권한
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        AuthUser authNormalUser = new AuthUser(currentUser.getId(), currentUser.getEmail(), currentUser.getUserRole());

        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "중화반점");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(12, 0));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice", 18000);
        ReflectionTestUtils.setField(storeRequestDto, "notice", "공지입니다");

        // 가게는 정상적으로 존재
        Store store = new Store(storeRequestDto, currentUser);
        ReflectionTestUtils.setField(store, "id", 1L);

        // 메뉴 등록 요청
        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("짬뽕", 7000L);

        // Mock 설정: userRepository에서 normalUser를 반환
        given(userRepository.findById(currentUser.getId())).willReturn(Optional.of(currentUser));

        // when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            menuService.createMenu(store.getId(), createMenuRequestDto, authNormalUser);
        });

        // 예외 메시지 검증
        assertEquals("계정의 권한이 없습니다.", exception.getMessage());

        // Verify repository calls
        verify(userRepository, times(1)).findById(currentUser.getId());
    }

    @Test
    void menu등록_실패_본인가게가_아닌경우(){
        // given
        User ownerUser = new User("owner@example.com", "password", UserRole.OWNER); // 가게 주인
        ReflectionTestUtils.setField(ownerUser, "id", 1L);

        User otherUser = new User("other@example.com", "password", UserRole.OWNER); // 다른 가게 주인
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        AuthUser authOtherUser = new AuthUser(otherUser.getId(), otherUser.getEmail(), otherUser.getUserRole());

        // 가게 정보 설정
        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "중화반점2");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(13, 30));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(23, 30));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice", 20000);
        ReflectionTestUtils.setField(storeRequestDto, "notice", "공지");

        // ownerUser가 주인인 가게
        Store store = new Store(storeRequestDto, ownerUser);
        ReflectionTestUtils.setField(store, "id", 1L);

        // 메뉴 등록 요청
        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("울면", 9000L);

        // Mock 설정: otherUser는 현재 사용자이고 store의 주인이 아님
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));

        // when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            menuService.createMenu(store.getId(), createMenuRequestDto, authOtherUser);
        });

        // 예외 메시지 검증
        assertEquals("계정의 권한이 없습니다.", exception.getMessage());

        // 리포지토리 호출 검증
        verify(userRepository, times(1)).findById(otherUser.getId());
        verify(storeRepository, times(1)).findById(store.getId());
    }

    @Test
    void update_정상적으로_작동한다(){
        //given
        User user = new User("email", "password", UserRole.OWNER);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "중화반점");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(13,30));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(23,30));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice", 20000);
        Store store = new Store(storeRequestDto, user);
        Long storeId = store.getId();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("짬뽕", 7000L);
        Menu menu = new Menu(createMenuRequestDto, store);
        Long menuId = menu.getId();

        given(menuRepository.findByIdAndStoreId(menuId, storeId)).willReturn(Optional.of(menu));

        CreateMenuRequestDto.UpdateMenuRequestDto updateMenuRequestDto = new CreateMenuRequestDto.UpdateMenuRequestDto("볶음밥", 8000L);

        AuthUser authUser = new AuthUser(user.getId(),"email", UserRole.OWNER);

        given(menuRepository.save(any(Menu.class))).willReturn(menu);

        //when
        UpdateMenuResponseDto updateMenuResponseDto = menuService.updateMenu(storeId, menuId, updateMenuRequestDto,authUser);

        //then
        Assertions.assertThat(updateMenuResponseDto.getMenuname()).isEqualTo("볶음밥");
        Assertions.assertThat(updateMenuResponseDto.getPrice()).isEqualTo(8000L);
    }

    @Test
    void menu수정_실패_본인가게가_아닌경우(){
        // given
        User ownerUser = new User("owner@example.com", "password", UserRole.OWNER); // 가게 주인
        ReflectionTestUtils.setField(ownerUser, "id", 1L);

        User otherUser = new User("other@example.com", "password", UserRole.OWNER); // 다른 가게 주인
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        AuthUser authOtherUser = new AuthUser(otherUser.getId(), otherUser.getEmail(), otherUser.getUserRole());

        // 가게 정보 설정
        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "중화반점2");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(13, 30));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(23, 30));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice", 20000);
        ReflectionTestUtils.setField(storeRequestDto, "notice", "공지");

        // ownerUser가 주인인 가게
        Store store = new Store(storeRequestDto, ownerUser);
        ReflectionTestUtils.setField(store, "id", 1L);

        // 메뉴 정보 설정
        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("울면", 9000L);
        Menu menu = new Menu(createMenuRequestDto, store);
        ReflectionTestUtils.setField(menu, "id", 1L);

        // 메뉴 수정 요청
        CreateMenuRequestDto.UpdateMenuRequestDto updateMenuRequestDto = new CreateMenuRequestDto.UpdateMenuRequestDto("볶음밥", 8000L);

        // Mock 설정: otherUser는 현재 사용자이고 store의 주인이 아님
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));

        // when - then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            menuService.updateMenu(store.getId(), menu.getId(), updateMenuRequestDto, authOtherUser);
        });

        // 예외 메시지 검증
        assertEquals("계정의 권한이 없습니다.", exception.getMessage());

        verify(userRepository, times(1)).findById(otherUser.getId());
        verify(storeRepository, times(1)).findById(store.getId());
    }

    @Test
    void menu_정상적으로_삭제(){
        //given
        User user = new User("email", "password", UserRole.OWNER);
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        StoreRequestDto storeRequestDto = new StoreRequestDto();
        ReflectionTestUtils.setField(storeRequestDto, "name", "중화반점");
        ReflectionTestUtils.setField(storeRequestDto, "openTime", LocalTime.of(13,30));
        ReflectionTestUtils.setField(storeRequestDto, "closeTime", LocalTime.of(23,30));
        ReflectionTestUtils.setField(storeRequestDto, "minPrice", 20000);
        Store store = new Store(storeRequestDto, user);
        Long storeId = store.getId();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        CreateMenuRequestDto createMenuRequestDto = new CreateMenuRequestDto("짜장면", 6000L);
        Menu menu = new Menu(createMenuRequestDto, store);
        Long menuId = menu.getId();
        given(menuRepository.findByIdAndStoreId(menuId, storeId)).willReturn(Optional.of(menu));

        //when
        menuService.deleteMenu(storeId, menuId, authUser);

        //then
        assertEquals(true, menu.getDeleted());
    }
}
