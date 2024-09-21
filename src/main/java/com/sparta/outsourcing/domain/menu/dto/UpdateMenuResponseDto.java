package com.sparta.outsourcing.domain.menu.dto;

import com.sparta.outsourcing.domain.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateMenuResponseDto {

    private Long id;
    private String menuname;
    private Long price;
    private LocalDateTime modifiedAt;

    public UpdateMenuResponseDto(Menu menu){
        this.id = menu.getId();
        this.menuname = menu.getName();
        this.price = menu.getPrice();
        this.modifiedAt = menu.getModifiedAt();
    }
}
