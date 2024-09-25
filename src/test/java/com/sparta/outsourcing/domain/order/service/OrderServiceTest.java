package com.sparta.outsourcing.domain.order.service;

import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MenuRepository menuRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("주문 생성 - 유효한 요청 - 주문 응답 DTO 반환")
    void createOrder_ValidRequest_ReturnsOrderResponseDto() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_PLACED);
        requestDto.setTotalPrice(10000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(10000);

        when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(java.util.Optional.of(menu));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When
            OrderResponseDto responseDto = orderService.createOrder(requestDto, UserRole.USER);

            // Then
            assertNotNull(responseDto);
            assertEquals(1L, responseDto.getId());
            assertEquals(OrderStatus.ORDER_PLACED, responseDto.getStatus());
            assertEquals(10000, responseDto.getTotalPrice());
            verify(orderRepository).save(any(Orders.class));
        }
    }

    @Test
    @DisplayName("주문 생성 - 유효한 요청 - 주문 응답 DTO 반환")
    void createOrder_OwnerRole_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();

        // When & Then
        assertThrows(ApplicationException.class, () -> {
            orderService.createOrder(requestDto, UserRole.OWNER);
        });
    }

    @Test
    @DisplayName("주문 생성 - 유효하지 않은 상태값으로 생성 시도")
    void createOrder_InvalidStatus_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_CONFIRMED);
        requestDto.setTotalPrice(15000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.createOrder(requestDto, UserRole.USER);
            });
            assertEquals(ErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("주문 생성 - 고객이 존재하지 않는 경우 예외 발생")
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(100L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_PLACED);
        requestDto.setTotalPrice(10000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        User customer = new User();
        // 고객이 존재하지 않는 경우
        ReflectionTestUtils.setField(customer, "id", 100L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(10000);

        when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(java.util.Optional.of(menu));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        // 모킹 영역 밖에서 시간 객체 생성
        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.createOrder(requestDto, UserRole.USER);
            });
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("주문 생성 - 가게가 영업시간이 아닌 경우 예외 발생")
    void createOrder_StoreClosed_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_PLACED);
        requestDto.setTotalPrice(10000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(9, 0));

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(10000);

        when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(java.util.Optional.of(menu));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.createOrder(requestDto, UserRole.USER);
            });
            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("주문 생성 - 최소 주문 금액 미달")
    void createOrder_MinOrderAmountNotMet_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_PLACED);
        requestDto.setTotalPrice(-10000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));


        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(10000);

        when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(java.util.Optional.of(menu));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.createOrder(requestDto, UserRole.USER);
            });
            assertEquals(ErrorCode.MINIMUM_ORDER_AMOUNT_NOT_MET, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("주문 생성 - 일반 유저가 주문 요청 시 성공")
    void createOrder_UserRole_Success() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_PLACED);
        requestDto.setTotalPrice(10000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setCustomer(customer);
        order.setStore(store);
        order.setMenu(menu);
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(10000);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When
            OrderResponseDto responseDto = orderService.createOrder(requestDto, UserRole.USER);

            // Then
            assertNotNull(responseDto);
            assertEquals(1L, responseDto.getId());
            assertEquals(OrderStatus.ORDER_PLACED, responseDto.getStatus());
            assertEquals(10000, responseDto.getTotalPrice());
            verify(orderRepository).save(any(Orders.class));
        }
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 유효한 요청 - 업데이트된 주문 응답 DTO 반환")
    void updateOrderStatus_ValidRequest_ReturnsUpdatedOrderResponseDto() {
        // Given
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CONFIRMED;
        UserRole userRole = UserRole.OWNER;
        Long userId = 1L;

        Orders existingOrder = new Orders();
        ReflectionTestUtils.setField(existingOrder, "id", 1L);
        existingOrder.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(store, "owner", owner);
        ReflectionTestUtils.setField(owner, "id", 1L);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        existingOrder.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        existingOrder.setMenu(menu);

        existingOrder.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenReturn(existingOrder);

        // When
        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, newStatus, userRole, userId);

        // Then
        assertNotNull(responseDto);
        assertEquals(newStatus, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 잘못된 상태 전환 - 예외 발생")
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        // Given
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_DELIVERED;
        UserRole userRole = UserRole.OWNER;
        Long userId = 1L;

        Orders existingOrder = new Orders();
        ReflectionTestUtils.setField(existingOrder, "id", 1L);
        existingOrder.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(store, "owner", owner);

        existingOrder.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));

        // When & Then
        assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });
    }


    @Test
    @DisplayName("주문 생성 검증 - 유효한 상태값")
    void validateOrderCreationRequest_ValidStatus_DoesNotThrowException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStatus(OrderStatus.ORDER_PLACED);

        // When & Then
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderCreationRequest", requestDto);
        });
    }

    @Test
    @DisplayName("주문 생성 검증 - 유효하지 않은 상태값")
    void validateOrderCreationRequest_InvalidStatus_ThrowsException() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStatus(OrderStatus.ORDER_CONFIRMED);

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderCreationRequest", requestDto);
        });
        assertEquals(ErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 유효성 검증 - 가게가 존재하는 경우")
    void getValidStore_StoreExists_ReturnsStore() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);

        // When
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        Store result = ReflectionTestUtils.invokeMethod(orderService, "getValidStore", storeId);

        // Then
        assertNotNull(result);
        assertEquals(storeId, result.getId());
    }

    @Test
    @DisplayName("가게 유효성 검증 - 가게가 존재하지 않는 경우")
    void getValidStore_StoreNotExists_ThrowsException() {

        // Given
        Long storeId = 100L;

        // When
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "getValidStore", storeId);
        });
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("고객 유효성 검증 - 고객이 존재하는 경우")
    void getValidCustomer_CustomerExists_ReturnsCustomer() {

        // Given
        Long userId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        // When
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = ReflectionTestUtils.invokeMethod(orderService, "getValidCustomer", userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    @DisplayName("고객 유효성 검증 - 고객이 존재하지 않는 경우")
    void getValidCustomer_CustomerNotExists_ThrowsException() {
        Long userId = 100L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "getValidCustomer", userId);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 유효성 검증 - 메뉴가 존재하는 경우")
    void getValidMenu_MenuExists_ReturnsMenu() {
        Long menuId = 1L;
        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", menuId);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        Menu result = ReflectionTestUtils.invokeMethod(orderService, "getValidMenu", menuId);

        assertNotNull(result);
        assertEquals(menuId, result.getId());
    }

    @Test
    @DisplayName("메뉴 유효성 검증 - 메뉴가 존재하지 않는 경우")
    void getValidMenu_MenuNotExists_ThrowsException() {
        Long menuId = 100L;

        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "getValidMenu", menuId);
        });
        assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 유효성 검증 및 조회 - 가게가 존재하지 않는 경우 예외 발생")
    void getValidStore_StoreNotFound_ThrowsException() {
        // Given
        Long storeId = 100L;

        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "getValidStore", storeId);
        });
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 유효성 검증 및 조회 - 유효한 가게 반환")
    void getValidStore_ValidStore_ReturnsStore() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // When
        Store result = ReflectionTestUtils.invokeMethod(orderService, "getValidStore", storeId);

        // Then
        assertNotNull(result);
        assertEquals(storeId, result.getId());
    }

    @Test
    @DisplayName("주문 상태 변경 가능한 목록 조회 - 기본 상태 테스트")
    void getAvailableStatusChanges_DefaultState_ReturnsValidStatusList() {
        List<OrderStatus> statusList = ReflectionTestUtils.invokeMethod(orderService, "getAvailableStatusChanges", OrderStatus.ORDER_PLACED);

        assertNotNull(statusList);
        assertTrue(statusList.contains(OrderStatus.ORDER_CONFIRMED));
    }

    @Test
    @DisplayName("주문 상태 변경 가능한 목록 조회 - 유효하지 않은 상태")
    void getAvailableStatusChanges_InvalidState_ReturnsEmptyList() {
        List<OrderStatus> statusList = ReflectionTestUtils.invokeMethod(orderService, "getAvailableStatusChanges", OrderStatus.ORDER_CANCELED);

        assertNotNull(statusList);
        assertTrue(statusList.isEmpty());
    }

    @Test
    @DisplayName("주문 금액 검증 - 최소 금액 이상일 경우 성공")
    void validateOrderAmount_MinPrice_Success() {
        Store store = new Store();
        ReflectionTestUtils.setField(store, "minPrice", 5000);

        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderAmount", 10000, store);
        });
    }

    @Test
    @DisplayName("주문 금액 검증 - 최소 금액 미달 시 예외 발생")
    void validateOrderAmount_BelowMinPrice_ThrowsException() {
        Store store = new Store();
        ReflectionTestUtils.setField(store, "minPrice", 5000);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderAmount", 3000, store);
        });
        assertEquals(ErrorCode.MINIMUM_ORDER_AMOUNT_NOT_MET, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 금액 검증 - 최소 주문 금액 이상")
    void validateOrderAmount_ValidAmount_DoesNotThrowException() {
        Store store = new Store();
        ReflectionTestUtils.setField(store, "minPrice", 5000);

        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderAmount", 10000, store);
        });
    }

    @Test
    @DisplayName("주문 금액 검증 - 최소 주문 금액 미만")
    void validateOrderAmount_InvalidAmount_ThrowsException() {
        Store store = new Store();
        ReflectionTestUtils.setField(store, "minPrice", 5000);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "validateOrderAmount", 3000, store);
        });
        assertEquals(ErrorCode.MINIMUM_ORDER_AMOUNT_NOT_MET, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 가능한 상태 전환 시도")
    void updateOrderStatus_ValidTransitions_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CONFIRMED, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.ORDER_CONFIRMED, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 잘못된 상태 전환 시 예외 발생")
    void updateOrderStatus_InvalidTransitions_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_DELIVERED, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 생성 - 유효하지 않은 입력 값으로 인해 예외 발생")
    void createOrder_InvalidInputs_ThrowsException() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setStoreId(1L);
        requestDto.setCustomerId(1L);
        requestDto.setMenuId(1L);
        requestDto.setStatus(OrderStatus.ORDER_CONFIRMED);
        requestDto.setTotalPrice(1000);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "minPrice", 5000);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.createOrder(requestDto, UserRole.USER);
        });
        assertEquals(ErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 주문 취소 - 성공")
    void updateOrderStatus_CancelOrder_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CANCELED, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.ORDER_CANCELED, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 주문 취소 - 잘못된 상태 전환 (ORDER_CONFIRMED)")
    void updateOrderStatus_CancelOrder_InvalidTransitionFromConfirmed_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_CONFIRMED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CANCELED, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_CONFIRMED_TO_CANCELLED, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 주문 취소 - 잘못된 상태 전환 (ORDER_PREPARING)")
    void updateOrderStatus_CancelOrder_InvalidTransitionFromPreparing_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PREPARING);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CANCELED, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 상태 변경 시 성공")
    void updateOrderStatus_OwnerRole_Success() {
        Long orderId = 1L;
        Long userId = 2L;
        UserRole userRole = UserRole.OWNER;
        OrderStatus newStatus = OrderStatus.ORDER_CONFIRMED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);

        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, newStatus, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(newStatus, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 고객이 주문 취소 시 성공")
    void updateOrderStatus_CustomerCancel_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, newStatus, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(newStatus, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 아닌 사용자가 상태 변경 시 예외 발생")
    void updateOrderStatus_NonOwnerRole_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        OrderStatus newStatus = OrderStatus.ORDER_CONFIRMED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.ORDER_STATUS_CHANGE_FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 아닌 사용자가 본인의 주문을 취소 시 성공")
    void updateOrderStatus_NonOwnerRole_CancelOrder_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, newStatus, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(newStatus, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 아닌 사용자가 다른 사용자의 주문을 취소 시 예외 발생")
    void updateOrderStatus_NonOwnerRole_CancelOtherUserOrder_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_USER_FOR_ORDER, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 이미 해당 상태로 변경된 경우 예외 발생")
    void updateOrderStatus_AlreadyChanged_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;
        OrderStatus newStatus = OrderStatus.ORDER_PLACED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.ALREADY_ORDER_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 전용 주문 조회 - 유효한 요청 - 주문 응답 DTO 반환")
    void getOrderForUser_ValidRequest_ReturnsOrderResponseDto() {
        Long orderId = 1L;
        Long tokenUserId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", tokenUserId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponseDto responseDto = orderService.getOrderForUser(orderId, tokenUserId, userRole);

        assertNotNull(responseDto);
        assertEquals(orderId, responseDto.getId());
    }

    @Test
    @DisplayName("유저 전용 주문 조회 - 주문이 존재하지 않는 경우 예외 발생")
    void getOrderForUser_OrderNotFound_ThrowsException() {
        Long orderId = 100L;
        Long tokenUserId = 1L;
        UserRole userRole = UserRole.USER;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderForUser(orderId, tokenUserId, userRole);
        });

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 전용 주문 조회 - 고객 ID가 일치하지 않는 경우 예외 발생")
    void getOrderForUser_CustomerIdMismatch_ThrowsException() {
        Long orderId = 1L;
        Long tokenUserId = 2L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderForUser(orderId, tokenUserId, userRole);
        });

        assertEquals(ErrorCode.ORDER_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 전용 주문 조회 - 권한이 USER가 아닌 경우 예외 발생")
    void getOrderForUser_InvalidUserRole_ThrowsException() {
        Long orderId = 1L;
        Long tokenUserId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", tokenUserId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderForUser(orderId, tokenUserId, userRole);
        });

        assertEquals(ErrorCode.ORDER_ACCESS_DENIED, exception.getErrorCode());
    }


@Test
@DisplayName("사장님 전용 주문 조회 - 유효한 요청 - 주문 응답 DTO 반환")
void getOrderByOwner_ValidRequest_ReturnsOrderResponseDto() {
    Long orderId = 1L;
    Long ownerId = 1L;
    UserRole userRole = UserRole.OWNER;

    Orders order = new Orders();
    ReflectionTestUtils.setField(order, "id", orderId);
    order.setStatus(OrderStatus.ORDER_PLACED);

    User customer = new User();
    ReflectionTestUtils.setField(customer, "id", 2L);
    order.setCustomer(customer);

    Store store = new Store();
    ReflectionTestUtils.setField(store, "id", 1L);
    order.setStore(store);

    User owner = new User();
    ReflectionTestUtils.setField(owner, "id", ownerId);
    ReflectionTestUtils.setField(store, "owner", owner);


    Menu menu = new Menu();
    ReflectionTestUtils.setField(menu, "id", 1L);
    order.setMenu(menu);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponseDto responseDto = orderService.getOrderByOwner(orderId, ownerId, userRole);

    assertNotNull(responseDto);
    assertEquals(orderId, responseDto.getId());
}

    @Test
    @DisplayName("사장님 전용 주문 조회 - 주문이 존재하지 않는 경우 예외 발생")
    void getOrderByOwner_OrderNotFound_ThrowsException() {
        Long orderId = 100L;
        Long ownerId = 1L;
        UserRole userRole = UserRole.OWNER;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderByOwner(orderId, ownerId, userRole);
        });

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사장님 전용 주문 조회 - 권한이 OWNER가 아닌 경우 예외 발생")
    void getOrderByOwner_InvalidUserRole_ThrowsException() {
        Long orderId = 1L;
        Long ownerId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderByOwner(orderId, ownerId, userRole);
        });

        assertEquals(ErrorCode.ORDER_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("사장님 전용 주문 조회 - 사장님 ID가 일치하지 않는 경우 예외 발생")
    void getOrderByOwner_OwnerIdMismatch_ThrowsException() {
        Long orderId = 1L;
        Long ownerId = 2L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.getOrderByOwner(orderId, ownerId, userRole);
        });

        assertEquals(ErrorCode.ORDER_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 주문 확인에서 주문 준비 중으로 변경 시 성공")
    void updateOrderStatus_ConfirmedToPreparing_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_CONFIRMED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, OrderStatus.ORDER_PREPARING, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.ORDER_PREPARING, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 주문 준비 중에서 배달 중으로 변경 시 성공")
    void updateOrderStatus_PreparingToOnTheWay_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PREPARING);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, OrderStatus.ORDER_ON_THE_WAY, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.ORDER_ON_THE_WAY, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 배달 중에서 배달 완료로 변경 시 성공")
    void updateOrderStatus_OnTheWayToDelivered_Success() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_ON_THE_WAY);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, OrderStatus.ORDER_DELIVERED, userRole, userId);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.ORDER_DELIVERED, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }


    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 본인의 가게 주문 상태 변경 시 성공")
    void updateOrderStatus_OwnerUpdatesOwnStoreOrder_Success() {
        Long orderId = 1L;
        Long ownerId = 1L;
        UserRole userRole = UserRole.OWNER;
        OrderStatus newStatus = OrderStatus.ORDER_CONFIRMED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 2L);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId, newStatus, userRole, ownerId);

        assertNotNull(responseDto);
        assertEquals(newStatus, responseDto.getStatus());
        verify(orderRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 사장님이 다른 가게 주문 상태 변경 시 예외 발생")
    void updateOrderStatus_OwnerUpdatesOtherStoreOrder_ThrowsException() {
        Long orderId = 1L;
        Long ownerId = 1L;
        UserRole userRole = UserRole.OWNER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CONFIRMED, userRole, ownerId);
        });

        assertEquals(ErrorCode.INVALID_OWNER_FOR_ORDER, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 유효하지 않은 상태 전환 시 예외 발생")
    void updateOrderStatus_InvalidStatusTransition_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;
        OrderStatus newStatus = OrderStatus.ORDER_DELIVERED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 취소 처리 - 잘못된 주문 상태 전이 시 예외 발생")
    void cancelOrder_InvalidOrderStatus_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;
        OrderStatus newStatus = OrderStatus.ORDER_DELIVERED;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, exception.getErrorCode());
    }








    @Test
    @DisplayName("가게 영업시간 검증 - 영업시간 내")
    void validateStoreOpenStatus_StoreOpen_DoesNotThrowException() {
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        LocalTime mockTime = LocalTime.of(10, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(orderService, "validateStoreOpenStatus", storeId);
            });
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업시간 외")
    void validateStoreOpenStatus_StoreClosed_ThrowsException() {
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        LocalTime mockTime = LocalTime.of(19, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                ReflectionTestUtils.invokeMethod(orderService, "validateStoreOpenStatus", storeId);
            });
            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("주문 응답 DTO 매핑 - 유효한 주문 객체")
    void mapToResponseDto_ValidOrder_ReturnsOrderResponseDto() {
        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(customer, "email", "customer@example.com");
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "name", "Test Store");
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "name", "Test Menu");
        ReflectionTestUtils.setField(menu, "price", 7000L);
        order.setMenu(menu);

        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(1000);

        OrderResponseDto responseDto = ReflectionTestUtils.invokeMethod(orderService, "mapToResponseDto", order);

        assertNotNull(responseDto);
        assertEquals(order.getId(), responseDto.getId());
        assertEquals(order.getCustomer().getId(), responseDto.getCustomerId());
        assertEquals(order.getCustomer().getEmail(), responseDto.getCustomerEmail());
        assertEquals(order.getStore().getId(), responseDto.getStoreId());
        assertEquals(order.getStore().getName(), responseDto.getStoreName());
        assertEquals(order.getMenu().getId(), responseDto.getMenuId());
        assertEquals(order.getMenu().getName(), responseDto.getMenuName());
        assertEquals(order.getMenu().getPrice(), responseDto.getMenuPrice());
        assertEquals(order.getStatus(), responseDto.getStatus());
        assertEquals(order.getTotalPrice(), responseDto.getTotalPrice());
    }

    @Test
    @DisplayName("주문 응답 DTO 매핑 - 주문 객체가 null인 경우")
    void mapToResponseDto_NullOrder_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "mapToResponseDto", (Orders) null);
        });
    }

    @Test
    @DisplayName("주문 응답 DTO 매핑 - 고객 정보가 없는 경우")
    void mapToResponseDto_NoCustomer_ThrowsException() {
        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "name", "Test Store");
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "name", "Test Menu");
        ReflectionTestUtils.setField(menu, "price", 7000L);
        order.setMenu(menu);

        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(1000);

        assertThrows(NullPointerException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "mapToResponseDto", order);
        });
    }

    @Test
    @DisplayName("주문 응답 DTO 매핑 - 가게 정보가 없는 경우")
    void mapToResponseDto_NoStore_ThrowsException() {
        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(customer, "email", "customer@example.com");
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "name", "Test Menu");
        ReflectionTestUtils.setField(menu, "price", 7000L);
        order.setMenu(menu);

        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(1000);

        assertThrows(NullPointerException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "mapToResponseDto", order);
        });
    }

    @Test
    @DisplayName("주문 응답 DTO 매핑 - 메뉴 정보가 없는 경우")
    void mapToResponseDto_NoMenu_ThrowsException() {
        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", 1L);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(customer, "email", "customer@example.com");
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "name", "Test Store");
        order.setStore(store);

        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setTotalPrice(1000);

        assertThrows(NullPointerException.class, () -> {
            ReflectionTestUtils.invokeMethod(orderService, "mapToResponseDto", order);
        });
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 배달 중에서 주문 취소 시 예외 발생")
    void updateOrderStatus_CancelOrder_InvalidTransitionFromOnTheWay_ThrowsException() {
        Long orderId = 1L;
        Long userId = 1L;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_ON_THE_WAY);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CANCELED, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED, exception.getErrorCode());
    }


    @Test
    @DisplayName("가게 영업시간 검증 - 자정을 넘는 경우 영업시간 외")
    void validateStoreOpenStatus_StoreClosedAfterMidnight_ThrowsException() {
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime now = LocalTime.of(3, 0);
        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(now);

            ApplicationException exception = assertThrows(ApplicationException.class, () -> orderService.validateStoreOpenStatus(storeId));
            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }


    @Test
    @DisplayName("가게 영업시간 검증 - 자정을 넘는 경우 영업시간 내")
    void validateStoreOpenStatus_StoreOpenAfterMidnight_DoesNotThrowException() {
        Long storeId = 1L;
        Store store = new Store();

        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(23, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(3, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime now = LocalTime.of(1, 0);
        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(now);

            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }



    @Test
    @DisplayName("가게 영업시간 검증 - 자정을 넘는 경우 영업시간 외 (오전 9시)")
    void validateStoreOpenStatus_StoreClosedAfterMidnight_Morning_ThrowsException() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(9, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }


    @Test
    @DisplayName("가게 영업시간 검증 - 자정을 넘는 경우 영업시간 내 (오후 11시)")
    void validateStoreOpenStatus_StoreOpenAfterMidnight_Evening_DoesNotThrowException() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(23, 0);

        // Mock current time to 11 PM
        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업 시작 전 (오전 8시)")
    void validateStoreOpenStatus_BeforeOpeningTime_ThrowsException() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(8, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }


    @Test
    @DisplayName("가게 영업시간 검증 - 영업 종료 후 (오후 7시)")
    void validateStoreOpenStatus_AfterClosingTime_ThrowsException() {
        // Given
        Long storeId = 2L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(19, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업 시간 내 (오후 2시)")
    void validateStoreOpenStatus_DuringOperatingHours_DoesNotThrowException() {
        // Given
        Long storeId = 3L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(14, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업 시작 시간 (오전 9시)")
    void validateStoreOpenStatus_AtOpeningTime_DoesNotThrowException() {
        // Given
        Long storeId = 4L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(9, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }


    @Test
    @DisplayName("가게 영업시간 검증 - 영업 종료 시간 (오후 6시)")
    void validateStoreOpenStatus_AtClosingTime_ThrowsException() {
        // Given
        Long storeId = 5L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(18, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업 시작 시간과 동일한 경우")
    void validateStoreOpenStatus_AtExactOpeningTime_DoesNotThrowException() {
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime now = LocalTime.of(9, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(now);

            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("가게 영업시간 검증 - 영업 종료 시간과 동일한 경우")
    void validateStoreOpenStatus_AtExactClosingTime_ThrowsException() {
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(18, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime now = LocalTime.of(18, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(now);

            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 시작 전 (오후 9시)")
    void validateStoreOpenStatus_OvernightStore_BeforeOpeningTime_ThrowsException() {
        // Given
        Long storeId = 1L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(21, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 시작 시간 (오후 10시)")
    void validateStoreOpenStatus_OvernightStore_AtOpeningTime_DoesNotThrowException() {
        // Given
        Long storeId = 2L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(22, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 중 시간 (오후 11시)")
    void validateStoreOpenStatus_OvernightStore_DuringOperatingHours_Evening_DoesNotThrowException() {
        // Given
        Long storeId = 3L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(23, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 중 시간 (오전 1시)")
    void validateStoreOpenStatus_OvernightStore_DuringOperatingHours_Morning_DoesNotThrowException() {
        // Given
        Long storeId = 4L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(1, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            assertDoesNotThrow(() -> orderService.validateStoreOpenStatus(storeId));
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 종료 시간 (오전 2시)")
    void validateStoreOpenStatus_OvernightStore_AtClosingTime_ThrowsException() {
        // Given
        Long storeId = 5L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(2, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("자정 넘는 영업시간 검증 - 영업 종료 후 (오전 3시)")
    void validateStoreOpenStatus_OvernightStore_AfterClosingTime_ThrowsException() {
        // Given
        Long storeId = 6L;
        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(store, "openTime", LocalTime.of(22, 0));
        ReflectionTestUtils.setField(store, "closeTime", LocalTime.of(2, 0));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalTime mockTime = LocalTime.of(3, 0);

        try (MockedStatic<LocalTime> mockedLocalTime = mockStatic(LocalTime.class)) {
            mockedLocalTime.when(LocalTime::now).thenReturn(mockTime);

            // When & Then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderService.validateStoreOpenStatus(storeId);
            });

            assertEquals(ErrorCode.STORE_CLOSED, exception.getErrorCode());
        }
    }





    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_CONFIRMED인 경우 예외 발생")
    void cancelOrder_WhenStatusIsConfirmed_ThrowsException() {
        // Given
        Long orderId = 4L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_CONFIRMED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_CONFIRMED_TO_CANCELLED, exception.getErrorCode());

        assertEquals(OrderStatus.ORDER_CONFIRMED, order.getStatus());

        verify(orderRepository, never()).save(any(Orders.class));
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_PREPARING인 경우 예외 발생")
    void cancelOrder_WhenStatusIsPreparing_ThrowsException() {
        // Given
        Long orderId = 5L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PREPARING);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED, exception.getErrorCode());

        assertEquals(OrderStatus.ORDER_PREPARING, order.getStatus());

        verify(orderRepository, never()).save(any(Orders.class));
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_DELIVERED가 아닌 경우 예외 발생하지 않음")
    void cancelOrder_WhenStatusIsNotDelivered_DoesNotThrowException() {
        // Given
        Long orderId = 8L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        // When & Then
        assertDoesNotThrow(() -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(OrderStatus.ORDER_CANCELED, order.getStatus());
        verify(orderRepository).save(order);
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_PREPARING인 경우 INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED 예외 발생")
    void cancelOrder_WhenStatusIsPreparing_ThrowsInvalidTransitionException() {
        // Given
        Long orderId = 9L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PREPARING);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_PREPARING_TO_CANCELLED, exception.getErrorCode());

        assertEquals(OrderStatus.ORDER_PREPARING, order.getStatus());

        verify(orderRepository, never()).save(any(Orders.class));
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_PLACED인 경우 성공적으로 취소")
    void cancelOrder_WhenStatusIsPlaced_Succeeds() {
        // Given
        Long orderId = 10L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_PLACED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        order.setStore(store);


        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        // When
        assertDoesNotThrow(() -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        // Then
        assertEquals(OrderStatus.ORDER_CANCELED, order.getStatus());
        verify(orderRepository).save(order);
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_ON_THE_WAY인 경우 INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED 예외 발생")
    void cancelOrder_WhenStatusIsOnTheWay_ThrowsException() {
        // Given
        Long orderId = 12L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_ON_THE_WAY);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        // Menu 설정
        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED, exception.getErrorCode());

        assertEquals(OrderStatus.ORDER_ON_THE_WAY, order.getStatus());

        verify(orderRepository, never()).save(any(Orders.class));
    }


    @Test
    @DisplayName("주문 상태가 ORDER_DELIVERED일 때 다른 상태로 전환 시 예외가 발생하는지 테스트")
    void testOrderDeliveredStatusThrowsException() {
        // Given
        Long orderId = 1L;
        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "status", OrderStatus.ORDER_DELIVERED);

        Store store = new Store();
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_CONFIRMED, UserRole.OWNER, 1L);
        });

        assertEquals(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, exception.getErrorCode());
    }


    @Test
    @DisplayName("주문 취소 - 주문 상태가 ORDER_DELIVERED인 경우 INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED 예외 발생")
    void cancelOrder_WhenStatusIsOnDelivered_ThrowsException() {
        // Given
        Long orderId = 12L;
        Long userId = 1L;
        OrderStatus newStatus = OrderStatus.ORDER_CANCELED;
        UserRole userRole = UserRole.USER;

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.ORDER_DELIVERED);

        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", userId);
        order.setCustomer(customer);

        Store store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 2L);
        ReflectionTestUtils.setField(store, "owner", owner);
        order.setStore(store);

        Menu menu = new Menu();
        ReflectionTestUtils.setField(menu, "id", 1L);
        order.setMenu(menu);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.updateOrderStatus(orderId, newStatus, userRole, userId);
        });

        assertEquals(ErrorCode.INVALID_TRANSITION_FROM_ORDER_DELIVERED_TO_CANCELLED, exception.getErrorCode());

        assertEquals(OrderStatus.ORDER_DELIVERED, order.getStatus());

        verify(orderRepository, never()).save(any(Orders.class));
    }




}
