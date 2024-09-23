package com.sparta.outsourcing.domain.order.dto.request;

import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {
    private Long customerId;
    private Long storeId;
    private Long menuId;
    private OrderStatus status;
    private Integer totalPrice;
    private String email;
    private UserRole userRole;

}