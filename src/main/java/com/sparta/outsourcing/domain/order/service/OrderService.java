package com.sparta.outsourcing.domain.order.service;

import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    // 주문 생성 로직 ========================================================================================
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, UserRole userRole) {

        // 사장님이 주문요청을 하는 경우 예외 처리
        if (userRole.equals(UserRole.OWNER)) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_CREATION_FOR_OWNER);
        }

        validateOrderCreationRequest(orderRequestDto);

        Store store = getValidStore(orderRequestDto.getStoreId());
        User customer = getValidCustomer(orderRequestDto.getCustomerId());
        Menu menu = getValidMenu(orderRequestDto.getMenuId());

        validateOrderAmountAndStoreStatus(orderRequestDto.getTotalPrice(), store);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(orderRequestDto.getStatus());
        order.setTotalPrice(orderRequestDto.getTotalPrice());

        Orders savedOrder = orderRepository.save(order);

        return mapToResponseDto(savedOrder);
    }

    // 주문 생성 검증 메서드 ===================================================================================
    private void validateOrderCreationRequest(OrderRequestDto orderRequestDto) {
        if (orderRequestDto.getStatus() != OrderStatus.ORDER_PLACED) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    // 가게 유효성 검증 및 조회 메서드 ========================================================================
    private Store getValidStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
    }

    // 고객 유효성 검증 및 조회 메서드 ========================================================================
    private User getValidCustomer(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    // 메뉴 유효성 검증 및 조회 메서드 ========================================================================
    private Menu getValidMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MENU_NOT_FOUND));
    }

    // 주문 금액 및 가게 상태 검증 메서드 =====================================================================
    private void validateOrderAmountAndStoreStatus(Integer totalPrice, Store store) {

       // 주문 금액이 최소 주문 금액보다 작은 경우 예외 처리
        if (totalPrice < store.getMinPrice()) {
            throw new ApplicationException(ErrorCode.MINIMUM_ORDER_AMOUNT_NOT_MET);
        }

        // 가게가 영업 중이 아닌 경우 예외 처리
        LocalTime now = LocalTime.now();
        if (now.isBefore(store.getOpenTime()) || now.isAfter(store.getCloseTime())) {
            throw new ApplicationException(ErrorCode.STORE_CLOSED);
        }

    }

    // 주문 상태 변경 로직 ========================================================================================
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status, UserRole userRole, Long userId) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // 주문 취소가 아닌 경우, 사장님만 상태 변경 가능
        if (!userRole.equals(UserRole.OWNER) && status != OrderStatus.ORDER_CANCELED) {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_CHANGE_FORBIDDEN);
        }

        // 사장님인 경우, 본인의 가게가 맞는지 검증
        if (userRole.equals(UserRole.OWNER) && !order.getStore().getOwner().getId().equals(userId)) {
            throw new ApplicationException(ErrorCode.INVALID_OWNER_FOR_ORDER);
        }

        // 고객이 주문을 취소하려는 경우, 해당 고객이 맞는지 검증
        if (status == OrderStatus.ORDER_CANCELED && !order.getCustomer().getId().equals(userId)) {
            throw new ApplicationException(ErrorCode.INVALID_USER_FOR_ORDER);
        }

        // 이미 해당 상태로 변경된 경우 예외 처리
        if (order.getStatus().equals(status)) {
            throw new ApplicationException(ErrorCode.ALREADY_ORDER_STATUS);
        }

        updateOrderStatusSequence(order, status);

        Orders updatedOrder = orderRepository.save(order);
        return mapToResponseDto(updatedOrder);
    }


    // 주문 상태 변경 메소드
    private void updateOrderStatusSequence(Orders order, OrderStatus status) {

        // 상태 변경: 주문 접수 → 주문 확인
        if (order.getStatus() == OrderStatus.ORDER_PLACED && status == OrderStatus.ORDER_CONFIRMED) {
            order.setStatus(OrderStatus.ORDER_CONFIRMED);
            return;
        }

        // 상태 변경: 주문 확인 → 주문 준비 중
        if (order.getStatus() == OrderStatus.ORDER_CONFIRMED && status == OrderStatus.ORDER_PREPARING) {
            order.setStatus(OrderStatus.ORDER_PREPARING);
            return;
        }

        // 상태 변경: 주문 준비 중 → 배달 중
        if (order.getStatus() == OrderStatus.ORDER_PREPARING && status == OrderStatus.ORDER_ON_THE_WAY) {
            order.setStatus(OrderStatus.ORDER_ON_THE_WAY);
            return;
        }

        // 상태 변경: 배달 중 → 배달 완료
        if (order.getStatus() == OrderStatus.ORDER_ON_THE_WAY && status == OrderStatus.ORDER_DELIVERED) {
            order.setStatus(OrderStatus.ORDER_DELIVERED);
            return;
        }

        // 주문 취소 처리
        if (status == OrderStatus.ORDER_CANCELED) {
            if (order.getStatus() == OrderStatus.ORDER_PLACED) {
                order.setStatus(OrderStatus.ORDER_CANCELED);
                return;
            }

            if (order.getStatus() == OrderStatus.ORDER_CONFIRMED) {
                throw new ApplicationException(ErrorCode.INVALID_TRANSITION_FROM_ORDER_CONFIRMED_TO_CANCELLED);
            }

            if (order.getStatus() == OrderStatus.ORDER_PREPARING) {
                throw new ApplicationException(ErrorCode.INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED);
            }

            if (order.getStatus() == OrderStatus.ORDER_ON_THE_WAY || order.getStatus() == OrderStatus.ORDER_DELIVERED) {
                throw new ApplicationException(ErrorCode.INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED);
            }

            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        // 주문 상태 변경 동작에 어긋날 경우 예외 처리
        throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
    }

    // 유저 전용 주문 조회 로직 ====================================================================================
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderForUser(Long orderId, Long tokenUserId, UserRole userRole) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // 주문의 고객 ID가 JWT에서 추출된 유저 ID와 일치하는지 확인
        if (!order.getCustomer().getId().equals(tokenUserId)) {
            throw new ApplicationException(ErrorCode.ORDER_ACCESS_DENIED); // 접근 권한 없음
        }

        // 권한 검증: 고객만 자신의 주문을 조회할 수 있음
        if (userRole != UserRole.USER) {
            throw new ApplicationException(ErrorCode.ORDER_ACCESS_DENIED); // 권한 없음 예외 처리
        }

        return mapToResponseDto(order);
    }

    // 사장님 전용 주문 조회 로직 ==================================================================================
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOwner(Long orderId, Long ownerId, UserRole userRole) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // 권한 검증: 사장님만 자신의 가게 주문을 조회할 수 있음
        if (userRole != UserRole.OWNER || !order.getStore().getOwner().getId().equals(ownerId)) {
            throw new ApplicationException(ErrorCode.ORDER_ACCESS_DENIED); // 권한 없음 예외 처리
        }

        return mapToResponseDto(order);
    }


    // 주문 엔티티 -> 주문 응답 DTO 매핑 ======================================================================
    private OrderResponseDto mapToResponseDto(Orders order) {
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(order.getId());
        responseDto.setCustomerId(order.getCustomer().getId());
        responseDto.setStoreId(order.getStore().getId());
        responseDto.setMenuId(order.getMenu().getId());
        responseDto.setStatus(order.getStatus());
        responseDto.setTotalPrice(order.getTotalPrice());

        // 취소 가능 여부 확인
        responseDto.setCanCancel(canUserCancelOrder(order));

        // 상태 변경 가능 목록 설정
        responseDto.setAvailableStatusChanges(getAvailableStatusChanges(order.getStatus()));

        return responseDto;
    }

    // 유저가 취소할 수 있는지 확인
    private boolean canUserCancelOrder(Orders order) {
        return order.getStatus() == OrderStatus.ORDER_PLACED || order.getStatus() == OrderStatus.ORDER_CONFIRMED;
    }

    // 상태 전환 규칙을 미리 정의한 Map
    private static final Map<OrderStatus, List<OrderStatus>> statusTransitions = Map.of(
            OrderStatus.ORDER_PLACED, List.of(OrderStatus.ORDER_CONFIRMED),
            OrderStatus.ORDER_CONFIRMED, List.of(OrderStatus.ORDER_PREPARING),
            OrderStatus.ORDER_PREPARING, List.of(OrderStatus.ORDER_ON_THE_WAY),
            OrderStatus.ORDER_ON_THE_WAY, List.of(OrderStatus.ORDER_DELIVERED)
    );

    // 가능한 상태 변경 목록 반환 메서드
    private List<OrderStatus> getAvailableStatusChanges(OrderStatus currentStatus) {
        return statusTransitions.getOrDefault(currentStatus, Collections.emptyList());
    }

}