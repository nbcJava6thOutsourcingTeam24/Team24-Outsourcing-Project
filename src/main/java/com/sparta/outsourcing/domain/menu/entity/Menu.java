package com.sparta.outsourcing.domain.menu.entity;

import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long price;
    private boolean status = false;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

//    public createMenu(String name, Long price, boolean status, Store store){
//        this.name = name;
//        this.price = price;
//        this.status = status;
//        this.store = store;
//    }

    public Menu(CreateMenuRequestDto createMenuRequestDto, Store store) {
        this.name = createMenuRequestDto.getMenuname();
        this.price = createMenuRequestDto.getPrice();
        this.store = store;
    }
}
