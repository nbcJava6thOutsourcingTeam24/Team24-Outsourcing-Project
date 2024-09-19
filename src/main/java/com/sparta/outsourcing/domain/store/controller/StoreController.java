package com.sparta.outsourcing.domain.store.controller;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/stores")
    public ResponseEntity<String> createStore(@RequestBody StoreRequestDto storeRequestDto) {
        storeService.createStore(storeRequestDto);
        return new ResponseEntity<>("가게 생성이 완료되었습니다", HttpStatus.CREATED);
    }
}
