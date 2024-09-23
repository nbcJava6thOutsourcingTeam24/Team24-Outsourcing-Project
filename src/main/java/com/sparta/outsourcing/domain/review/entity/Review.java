package com.sparta.outsourcing.domain.review.entity;

import com.sparta.outsourcing.domain.common.entity.Timestamped;
import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private Integer rating;

    @Column(length = 250)
    private String contents;
    private LocalDateTime createdDate;


}
