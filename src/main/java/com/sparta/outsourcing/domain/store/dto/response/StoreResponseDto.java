package com.sparta.outsourcing.domain.store.dto.response;

import com.sparta.outsourcing.domain.store.entity.Store;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class StoreResponseDto {

    private Long id;
    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minPrice;
//    private List<MenuReponseDto> menuList;

    private StoreResponseDto(Long id, String name, LocalTime openTime, LocalTime closeTime,
        Integer minPrice) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minPrice = minPrice;
    }

    public static StoreResponseDto from(Store store) {
        return new StoreResponseDto(
            store.getId(),
            store.getName(),
            store.getOpenTime(),
            store.getCloseTime(),
            store.getMinPrice()
        );
    }
}
