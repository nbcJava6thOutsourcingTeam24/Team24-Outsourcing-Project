package com.sparta.outsourcing.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequestDto {

    private String menuname;
    private Long price;

}
