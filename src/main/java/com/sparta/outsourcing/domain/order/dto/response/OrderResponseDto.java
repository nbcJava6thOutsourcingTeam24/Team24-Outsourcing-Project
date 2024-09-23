package com.sparta.outsourcing.domain.order.dto.response;

import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Long id;
    private Long customerId;
    private Long storeId;
    private Long menuId;
    private OrderStatus status;
    private Integer totalPrice;
    private boolean canCancel;
    private List<OrderStatus> availableStatusChanges;
}