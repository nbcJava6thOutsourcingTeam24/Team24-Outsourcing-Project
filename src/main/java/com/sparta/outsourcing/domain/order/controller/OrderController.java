package com.sparta.outsourcing.domain.order.controller;

import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.service.OrderService;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderRequestDto orderRequestDto,
            @RequestAttribute("role") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto, userRole);
        return ResponseEntity.status(201).body(orderResponseDto); // 201 Created
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(orderId, status, userRole, userId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/user/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderForUser(
            @PathVariable Long orderId,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.getOrderForUser(orderId, userId, userRole);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/owner/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderByOwner(
            @PathVariable Long orderId,
            @RequestAttribute("userId") Long ownerId,
            @RequestAttribute("role") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.getOrderByOwner(orderId, ownerId, userRole);
        return ResponseEntity.ok(orderResponseDto);
    }
}
