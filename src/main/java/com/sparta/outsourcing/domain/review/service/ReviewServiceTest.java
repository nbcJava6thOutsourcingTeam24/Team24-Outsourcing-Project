package com.sparta.outsourcing.domain.review.service;

import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.entity.Review;
import com.sparta.outsourcing.domain.review.repository.ReviewRepository;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReviewSuccess() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;

        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("Great service!");

        Orders order = new Orders();
        order.setId(1L);
        order.setStatus(OrderStatus.ORDER_DELIVERED);

        // when
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // then
        reviewService.createReview(reviewRequestDTO, userId, userRole);

        // verify
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReviewFailsForNonUserRole() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.OWNER;  // 잘못된 권한
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("Great service!");

        // when & then
        assertThrows(ApplicationException.class, () ->
                reviewService.createReview(reviewRequestDTO, userId, userRole), ErrorCode.INVALID_ROLE_FOR_REVIEW_CREATION.getMessage());
    }

    @Test
    void testCreateReviewFailsWhenOrderNotFound() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("Great service!");

        // when
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(ApplicationException.class, () ->
                reviewService.createReview(reviewRequestDTO, userId, userRole), ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void testCreateReviewFailsWhenReviewAlreadyExists() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("Great service!");

        Orders order = new Orders();
        order.setId(1L);
        order.setStatus(OrderStatus.ORDER_DELIVERED);

        // when
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(true);

        // then
        assertThrows(ApplicationException.class, () ->
                reviewService.createReview(reviewRequestDTO, userId, userRole), ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());
    }
}
