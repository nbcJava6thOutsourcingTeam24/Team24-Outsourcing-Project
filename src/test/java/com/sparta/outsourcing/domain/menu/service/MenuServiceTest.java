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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
}
