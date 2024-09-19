package com.sparta.outsourcing.domain.store.entity;

import com.sparta.outsourcing.domain.common.entity.Timestamped;
import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Store extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean status = false;
    private Integer minPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    public Store(StoreRequestDto storeRequestDto, User user) {
        this.name = storeRequestDto.getName();
        this.openTime = storeRequestDto.getOpenTime();
        this.closeTime = storeRequestDto.getCloseTime();
        this.minPrice = storeRequestDto.getMinPrice();
        this.owner = user;
    }
}
