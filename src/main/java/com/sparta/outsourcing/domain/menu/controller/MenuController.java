package com.sparta.outsourcing.domain.menu.controller;

import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.CreateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 생성
     * @param requestDto
     * @param authUser
     * @return 상태코드 201, 생성된 메뉴 정보
     */
    @PostMapping("{storeId}/menus")
    public ResponseEntity<CreateMenuResponseDto> createMenu (
            @PathVariable(value = "storeId") Long storeId,
            @RequestBody CreateMenuRequestDto createMenuRequestDto
    ){
        return ResponseEntity.ok(menuService.createMenu(storeId, createMenuRequestDto));
    }

    /**
     * 메뉴 수정
     * @param menuId
     * @param requestDto
     * @param authUser
     * @return 상태코드 200, 업데이트된 메뉴 정보
     */


    /**
     * 메뉴 삭제
     * @param menuId
     * @param authUser
     * @return 상태코드 200, 삭제된 메뉴 아이디
     */


}
