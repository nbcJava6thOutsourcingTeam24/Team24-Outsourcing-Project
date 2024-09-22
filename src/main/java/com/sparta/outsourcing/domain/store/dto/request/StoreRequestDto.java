package com.sparta.outsourcing.domain.store.dto.request;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreRequestDto {

    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minPrice;
    private String notice;
}
