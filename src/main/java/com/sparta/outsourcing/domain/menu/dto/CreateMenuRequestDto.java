package com.sparta.outsourcing.domain.menu.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMenuRequestDto {

    private String menuname;
    private Long price;

}
