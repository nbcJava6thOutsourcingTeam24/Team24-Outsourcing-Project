package com.sparta.outsourcing.domain.menu.entity;

import com.sparta.outsourcing.domain.common.entity.Timestamped;
import com.sparta.outsourcing.domain.menu.dto.CreateMenuRequestDto;
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

    public Menu(CreateMenuRequestDto createMenuRequestDto, Store store) {
        this.name = createMenuRequestDto.getMenuname();
        this.price = createMenuRequestDto.getPrice();
        this.store = store;

    }

}
