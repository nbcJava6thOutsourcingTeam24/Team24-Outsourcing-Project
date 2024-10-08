package com.sparta.outsourcing.domain.store.dto.response;

import com.sparta.outsourcing.domain.menu.dto.response.UpdateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.store.entity.Store;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class StoreResponseDto {

    private Long id;
    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minPrice;
    private String notice;
    private List<UpdateMenuResponseDto> menuList;

    public StoreResponseDto(Long id, String name, LocalTime openTime, LocalTime closeTime,
        Integer minPrice, String notice) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minPrice = minPrice;
        this.notice = notice;
    }

    public StoreResponseDto(Long id, String name, LocalTime openTime, LocalTime closeTime,
        Integer minPrice, String notice, List<UpdateMenuResponseDto> menuList) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minPrice = minPrice;
        this.notice = notice;
        this.menuList = menuList;
    }
}
