package com.sparta.outsourcing.domain.store.dto.response;

import com.sparta.outsourcing.domain.store.entity.Store;
import java.time.LocalTime;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class StoreResponseDto {

    private Long id;
    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minPrice;
//    private List<MenuReponseDto> menuList;

    public StoreResponseDto(Long id, String name, LocalTime openTime, LocalTime closeTime,
        Integer minPrice) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minPrice = minPrice;
    }
}
