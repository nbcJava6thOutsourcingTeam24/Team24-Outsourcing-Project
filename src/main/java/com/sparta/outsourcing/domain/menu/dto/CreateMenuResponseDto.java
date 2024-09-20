package com.sparta.outsourcing.domain.menu.dto;

import com.sparta.outsourcing.domain.menu.entity.Menu;
import lombok.Getter;

@Getter
public class CreateMenuResponseDto {

    private Long id;
    private Long storeId;
    private String menuname;
    private Integer price;

    public CreateMenuResponseDto(Menu menu){
        this.id = menu.getId();
        this.storeId = menu.getStore().getId();
        this.menuname = menu.getName();
        this.price = menu.getPrice();
    }

}
