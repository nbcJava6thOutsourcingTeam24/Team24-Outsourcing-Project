package com.sparta.outsourcing.domain.menu.dto.response;

import com.sparta.outsourcing.domain.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateMenuResponseDto {

    private Long id;
    private Long storeId;
    private String menuname;
    private Long price;
    private LocalDateTime createdAt;


    public CreateMenuResponseDto(Menu menu){
        this.id = menu.getId();
        this.storeId = menu.getStore().getId();
        this.menuname = menu.getName();
        this.price = menu.getPrice();
        this.createdAt = menu.getCreatedAt();
    }

}
