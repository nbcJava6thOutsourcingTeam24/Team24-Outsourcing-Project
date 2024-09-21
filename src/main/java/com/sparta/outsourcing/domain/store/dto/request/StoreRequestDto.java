package com.sparta.outsourcing.domain.store.dto.request;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class StoreRequestDto {

    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minPrice;
}
