package com.sparta.outsourcing.domain.store.controller;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;

    /**
     * 가게를 등록합니다,
     * 예외사항 : 최대 3개의 가게를 등록할 수 있으며, role이 owner인 경우에만 작성 가능합니다.
     * @param storeRequestDto name, opentime, closetime, minPrice를 입력받습니다
     * @return 완료 응답을 생성합니다.
     */
    @PostMapping("/stores")
    public ResponseEntity<String> createStore(@RequestBody StoreRequestDto storeRequestDto) {
        storeService.createStore(storeRequestDto);
        return new ResponseEntity<>("가게 생성이 완료되었습니다", HttpStatus.CREATED);
    }

    /**
     * 가게를 수정합니다
     * @param storeId storeid를 PathVariable로 받습니다,
     * @param storeRequestDto name, opentime, closetime, minprice를 입력받습니다.
     * @return 완료 응답을 생성합니다.
     */
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<String> updateStore(@PathVariable Long storeId, @RequestBody StoreRequestDto storeRequestDto) {
        storeService.updateStore(storeId, storeRequestDto);
        return new ResponseEntity<>("가게 정보가 수정되었습니다", HttpStatus.OK);
    }
}
