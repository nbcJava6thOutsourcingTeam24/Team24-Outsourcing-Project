package com.sparta.outsourcing.domain.menu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequestDto {

    private String menuname;
    private Long price;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMenuRequestDto {

        private String menuname;
        private Long price;

    }
}
