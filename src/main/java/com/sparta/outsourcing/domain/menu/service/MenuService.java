package com.sparta.outsourcing.domain.menu.service;

import com.sparta.outsourcing.domain.menu.dto.request.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.response.CreateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.dto.response.UpdateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public CreateMenuResponseDto createMenu(Long storeId, CreateMenuRequestDto createMenuRequestDto, AuthUser authUser) {
        // 유저 권한 확인
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getUserRole() != UserRole.OWNER) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        // 가게 상태 확인 - 폐업된 가게는 메뉴 수정 불가
        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!currentUser.equals(store.getOwner())){
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 메뉴 등록
        Menu menu = new Menu(createMenuRequestDto, store);

        // DB 저장 및 responseDto로 반환
        return new CreateMenuResponseDto(menuRepository.save(menu));
    }

    public UpdateMenuResponseDto updateMenu(Long storeId, Long menuId, CreateMenuRequestDto.UpdateMenuRequestDto updateMenuRequestDto, AuthUser authUser) {
        // 유저 권한 확인
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getUserRole() != UserRole.OWNER) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        // 가게 상태 확인 - 폐업된 가게는 메뉴 수정 불가
        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!currentUser.equals(store.getOwner())){
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 메뉴 조회
        Menu menu = menuRepository.findByIdAndStoreId(menuId, store.getId())
                .orElseThrow(()-> new ApplicationException(ErrorCode.MENU_NOT_FOUND));

        // 삭제 여부 확인
        if(menu.getDeleted()){
            throw new ApplicationException(ErrorCode.MENU_NOT_FOUND);
        }

        // 메뉴 수정
        menu.updateMenu(updateMenuRequestDto);

        // DB 저장 및 ResponseDto로 반환
        return new UpdateMenuResponseDto(menuRepository.save(menu));
    }

    public void deleteMenu(Long storeId, Long menuId, AuthUser authUser) {
        // 유저 권한 확인
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getUserRole() != UserRole.OWNER) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        // 가게 상태 확인 - 폐업된 가게는 메뉴 수정 불가
        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        // 삭제 여부 확인 - 삭제된 메뉴는 수정 불가
        if(!currentUser.equals(store.getOwner())){
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        // 메뉴 조회
        Menu menu = menuRepository.findByIdAndStoreId(menuId, store.getId())
                .orElseThrow(()-> new ApplicationException(ErrorCode.MENU_NOT_FOUND));

        // 삭제 여부 확인
        if(menu.getDeleted()){
            throw new ApplicationException(ErrorCode.MENU_NOT_FOUND);
        }

        // 메뉴 삭제
        menu.deleteMenu();
    }
}
