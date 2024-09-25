package com.sparta.outsourcing.domain.review.service;

import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.dto.ReviewResponseDTO;
import com.sparta.outsourcing.domain.review.entity.Review;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.review.repository.ReviewRepository;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void 리뷰_생성() {
        // given
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("음식이 맛있어요. 별점 5점");

        Store store = new Store();
        ReflectionTestUtils.setField(store, "name", "Example Store");

        Orders order = new Orders();
        order.setStore(store);

        // User 객체 생성 및 설정
        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L); // 고객의 ID 설정
        order.setCustomer(customer); // 주문에 고객 설정

        // 주문 상태 설정
        order.setStatus(OrderStatus.ORDER_DELIVERED);

        given(orderRepository.findById(reviewRequestDTO.getOrderId())).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderId(reviewRequestDTO.getOrderId())).willReturn(false);

        Review review = new Review();
        ReflectionTestUtils.setField(review, "id", 1L);
        review.setRating(reviewRequestDTO.getRating());
        review.setContent(reviewRequestDTO.getContent());
        review.setCreatedDate(LocalDateTime.now());
        review.setStore(store); // Store 설정 추가

        given(reviewRepository.save(any(Review.class))).willReturn(review);

        // when
        ReviewResponseDTO response = reviewService.createReview(reviewRequestDTO, 1L, UserRole.USER);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("음식이 맛있어요. 별점 5점", response.getContent());
    }


    @Test
    public void 리뷰_생성시_주문이_없다면_ORDER_NOT_FOUND_에러를_던진다() {
        // given
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);

        given(orderRepository.findById(reviewRequestDTO.getOrderId())).willReturn(Optional.empty());

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reviewService.createReview(reviewRequestDTO, 1L, UserRole.USER);
        });

        // 예외 메시지 출력
        System.out.println("발생한 예외 메시지: " + exception.getMessage());

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void 리뷰_생성시_이미_리뷰가_있다면_REVIEW_ALREADY_EXISTS_에러를_던진다() {
        // given
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);

        // Orders 객체 생성 및 고객 정보 설정
        Orders order = new Orders();
        User customer = new User();
        ReflectionTestUtils.setField(customer, "id", 1L); // 고객의 ID 설정
        order.setCustomer(customer); // 주문에 고객 설정

        // 주문 상태 설정
        ReflectionTestUtils.setField(order, "status", OrderStatus.ORDER_DELIVERED);

        given(orderRepository.findById(reviewRequestDTO.getOrderId())).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderId(reviewRequestDTO.getOrderId())).willReturn(true);

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reviewService.createReview(reviewRequestDTO, 1L, UserRole.USER);
        });

        // 예외 메시지 출력
        System.out.println("발생한 예외 메시지: " + exception.getMessage());

        // REVIEW_ALREADY_EXISTS 에러 코드 검증
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    public void 리뷰_생성시_사용자가_고객이_아니면_INVALID_ROLE_FOR_REVIEW_CREATION_에러를_던진다() {
        // given
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);

        // 고객이 아닌 관리자가 리뷰를 작성하려고 할 때
        UserRole nonCustomerRole = UserRole.OWNER;

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reviewService.createReview(reviewRequestDTO, 1L, nonCustomerRole);
        });
        // 예외 메시지 출력
        System.out.println("발생한 예외 메시지: " + exception.getMessage());

        assertEquals(ErrorCode.INVALID_ROLE_FOR_REVIEW_CREATION, exception.getErrorCode());
    }





}