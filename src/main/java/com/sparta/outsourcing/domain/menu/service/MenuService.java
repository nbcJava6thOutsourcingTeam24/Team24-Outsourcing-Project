package com.sparta.outsourcing.domain.menu.service;

import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.CreateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    public CreateMenuResponseDto createMenu(Long storeId, CreateMenuRequestDto createMenuRequestDto) {
        // 유저 권한 확인

        // 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new IllegalArgumentException(("가게를 찾을 수 없습니다.")));

        // 메뉴 등록
        Menu menu = new Menu(createMenuRequestDto, store);

        // DB 저장 및 responseDto로 반환
        return new CreateMenuResponseDto(menuRepository.save(menu));
    }
}
