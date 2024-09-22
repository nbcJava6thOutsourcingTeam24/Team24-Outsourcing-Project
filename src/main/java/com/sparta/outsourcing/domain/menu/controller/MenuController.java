package com.sparta.outsourcing.domain.menu.controller;

import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.UpdateMenuRequestDto;
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
     * @param storeId
     * @param createMenuRequestDto
     * * @param authUser
     * @return 생성된 메뉴 정보
     */
    @PostMapping("{storeId}/menus")
    public ResponseEntity<String> createMenu (
            @PathVariable(value = "storeId") Long storeId,
            @RequestBody CreateMenuRequestDto createMenuRequestDto,
            @Auth AuthUser authUser
    ){
        menuService.createMenu(storeId, createMenuRequestDto, authUser);
        return new ResponseEntity<>("메뉴 생성이 완료되었습니다.", HttpStatus.CREATED);
    }

    /**
     * 메뉴 수정
     * @param storeId
     * @param menuId
     * @param updateMenuRequestDto
     * @param authUser
     * @return 업데이트된 메뉴 정보
     */
    @PutMapping("/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<String> updateMenu(
            @Auth AuthUser authUser,
            @PathVariable(value = "storeId") Long storeId,
            @PathVariable(value = "menuId") Long menuId,
            @RequestBody UpdateMenuRequestDto updateMenuRequestDto
    ){
        menuService.updateMenu(storeId, menuId, updateMenuRequestDto, authUser);
        return new ResponseEntity<>("메뉴 수정이 완료되었습니다.", HttpStatus.OK);
    }


    /**
     * 메뉴 삭제 (SOFT - DELETE로 구현)
     * @param storeId
     * @param menuId
     * @param authUser
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("stores/{storeId}/menus/{menuId}")
    public ResponseEntity<String> deleteMenu(
            @Auth AuthUser authUser,
            @PathVariable(value = "storeId") Long storeId,
            @PathVariable(value = "menuId") Long menuId
    ){
        menuService.deleteMenu(storeId, menuId, authUser);
        return new ResponseEntity<>("메뉴 삭제가 완료되었습니다.", HttpStatus.OK);
    }


}
