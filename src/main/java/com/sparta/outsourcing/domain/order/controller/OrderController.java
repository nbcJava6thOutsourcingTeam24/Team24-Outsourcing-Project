package com.sparta.outsourcing.domain.order.controller;

import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.service.OrderService;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto, @RequestAttribute("userRole") String userRoleStr) {
        UserRole userRole = UserRole.valueOf(userRoleStr);
        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto, userRole);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("userRole") String userRoleStr) {

        UserRole userRole = UserRole.valueOf(userRoleStr);

        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(orderId, status, userRole, userId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/user/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderForUser(@PathVariable Long orderId, @RequestParam Long userId, @RequestAttribute("userRole") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.getOrderForUser(orderId, userId, userRole);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/owner/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderByOwner(@PathVariable Long orderId, @RequestParam Long ownerId, @RequestAttribute("userRole") UserRole userRole) {
        OrderResponseDto orderResponseDto = orderService.getOrderByOwner(orderId, ownerId, userRole);
        return ResponseEntity.ok(orderResponseDto);
    }

}