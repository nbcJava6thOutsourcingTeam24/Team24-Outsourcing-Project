package com.sparta.outsourcing.domain.store.controller;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.dto.response.StoreResponseDto;
import com.sparta.outsourcing.domain.store.service.StoreService;
import com.sparta.outsourcing.domain.user.config.annotation.Auth;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;

    /**
     * 가게를 등록합니다, 예외사항 : 최대 3개의 가게를 등록할 수 있으며, role이 owner인 경우에만 작성 가능합니다.
     *
     * @param storeRequestDto name, opentime, closetime, minPrice를 입력받습니다
     * @return 완료 응답을 생성합니다.
     */
    @PostMapping("/stores")
    public ResponseEntity<String> createStore(@Auth AuthUser authUser,
        @RequestBody StoreRequestDto storeRequestDto) {
        storeService.createStore(authUser, storeRequestDto);
        return new ResponseEntity<>("가게 생성이 완료되었습니다", HttpStatus.CREATED);
    }

    /**
     * 가게를 수정합니다
     *
     * @param storeId         storeid를 PathVariable로 받습니다,
     * @param storeRequestDto name, opentime, closetime, minprice를 입력받습니다.
     * @return 완료 응답을 생성합니다.
     */
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<String> updateStore(@Auth AuthUser authUser, @PathVariable Long storeId,
        @RequestBody StoreRequestDto storeRequestDto) {
        storeService.updateStore(authUser, storeId, storeRequestDto);
        return new ResponseEntity<>("가게 정보가 수정되었습니다", HttpStatus.OK);
    }

    /**
     * 가게 목록을 조회합니다
     *
     * @return 가게 목록의 정보와 메뉴목록을 반환합니다.
     */
    @GetMapping("/stores")
    public ResponseEntity<List<StoreResponseDto>> getStoreList(@RequestParam String name) {
        List<StoreResponseDto> responseList = storeService.getStoreList(name);
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    /**
     * 가게를 조회합니다
     *
     * @param storeId 가게의 ID를 이용해 조회합니다
     * @return
     */
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long storeId) {
        return new ResponseEntity<>(storeService.getStore(storeId), HttpStatus.OK);
    }

    /**
     * 가게를 삭제합니다, SOFT - DELETE로 구현을 진행하였습니다.
     *
     * @param storeId 가게의 ID를 이용하여 삭제합니다
     * @return 삭제 완료 메세지를 출력합니다.
     */
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<String> deleteStore(@Auth AuthUser authUser, @PathVariable Long storeId) {
        storeService.deleteStore(authUser, storeId);
        return new ResponseEntity<>("가게 폐업처리가 완료되었습니다.", HttpStatus.OK);
    }
}
