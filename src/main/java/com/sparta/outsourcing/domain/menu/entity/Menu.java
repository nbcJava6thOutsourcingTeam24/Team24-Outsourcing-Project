package com.sparta.outsourcing.domain.menu.entity;

import com.sparta.outsourcing.domain.common.entity.Timestamped;
import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.menu.dto.UpdateMenuRequestDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Menu extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long price;
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    // 메뉴 생성
    public Menu(CreateMenuRequestDto createMenuRequestDto, Store store) {
        this.name = createMenuRequestDto.getMenuname();
        this.price = createMenuRequestDto.getPrice();
        this.store = store;
    }

    // 메뉴 수정
    public void updateMenu(UpdateMenuRequestDto updateMenuRequestDto) {
        this.name = updateMenuRequestDto.getMenuname();
        this.price = updateMenuRequestDto.getPrice();
    }

    // 메뉴 삭제
    public void deleteMenu(){
        this.deleted = true;
    }
}
