package com.sparta.outsourcing.domain.order.controller;

import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.service.OrderService;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderSuccessfully() {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderResponseDto responseDto = new OrderResponseDto();
        when(orderService.createOrder(any(OrderRequestDto.class), eq(UserRole.USER))).thenReturn(responseDto);

        ResponseEntity<OrderResponseDto> response = orderController.createOrder(requestDto, UserRole.USER);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void updateOrderStatusSuccessfully() {
        OrderResponseDto responseDto = new OrderResponseDto();
        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.ORDER_CONFIRMED), eq(UserRole.OWNER), eq(1L))).thenReturn(responseDto);

        ResponseEntity<OrderResponseDto> response = orderController.updateOrderStatus(1L, OrderStatus.ORDER_CONFIRMED, 1L, UserRole.OWNER);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getOrderForUserSuccessfully() {
        OrderResponseDto responseDto = new OrderResponseDto();
        when(orderService.getOrderForUser(eq(1L), eq(1L), eq(UserRole.USER))).thenReturn(responseDto);

        ResponseEntity<OrderResponseDto> response = orderController.getOrderForUser(1L, 1L, UserRole.USER);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getOrderByOwnerSuccessfully() {
        OrderResponseDto responseDto = new OrderResponseDto();
        when(orderService.getOrderByOwner(eq(1L), eq(1L), eq(UserRole.OWNER))).thenReturn(responseDto);

        ResponseEntity<OrderResponseDto> response = orderController.getOrderByOwner(1L, 1L, UserRole.OWNER);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }
}