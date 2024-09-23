package com.sparta.outsourcing.domain.review.service;

import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.dto.ReviewResponseDTO;
import com.sparta.outsourcing.domain.review.entity.Review;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void 리뷰_생성에_성공한다() {
        // given
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setOrderId(1L);
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setContent("Great service!");

        Orders order = new Orders();
        ReflectionTestUtils.setField(order, "status", "COMPLETED");

        given(orderRepository.findById(reviewRequestDTO.getOrderId())).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderId(reviewRequestDTO.getOrderId())).willReturn(false);

        Review review = new Review();
        ReflectionTestUtils.setField(review, "id", 1L);
        review.setRating(reviewRequestDTO.getRating());
        review.setContent(reviewRequestDTO.getContent());
        review.setCreatedDate(LocalDateTime.now());

        given(reviewRepository.save(any(Review.class))).willReturn(review);

        // when
        ReviewResponseDTO response = reviewService.createReview(reviewRequestDTO, 1L, UserRole.USER);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("Great service!", response.getContent());
    }
}
