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
    private String customerEmail;
    private Long storeId;
    private String storeName;
    private Long menuId;
    private String menuName;
    private Long menuPrice;
    private OrderStatus status;
    private Integer totalPrice;
    private boolean canUserCancel;
    private boolean canOwnerCancel;
    private List<OrderStatus> availableStatusChanges;

}