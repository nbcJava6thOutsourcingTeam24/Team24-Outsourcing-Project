package com.sparta.outsourcing.domain.menu.controller;

import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.service.MenuService;
import com.sparta.outsourcing.domain.user.config.annotation.Auth;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 생성
     * @param createMenuRequestDto
     * @param authUser
     * @return 상태코드 201, 생성된 메뉴 정보
     */
    @PostMapping("{storeId}/menus")
    public ResponseEntity<String> createMenu (
            @Auth AuthUser authUser,
            @PathVariable(value = "storeId") Long storeId,
            @RequestBody CreateMenuRequestDto createMenuRequestDto
    ){
        menuService.createMenu(storeId, createMenuRequestDto, authUser);
        return new ResponseEntity<>("메뉴 생성이 완료되었습니다", HttpStatus.CREATED);
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
