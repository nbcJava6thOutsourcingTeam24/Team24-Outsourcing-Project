package com.sparta.outsourcing.domain.review.service;

import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.enums.OrderStatus;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.dto.ReviewResponseDTO;
import com.sparta.outsourcing.domain.review.entity.Review;
import com.sparta.outsourcing.domain.review.repository.ReviewRepository;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    // 리뷰 생성 로직
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO, Long userId, UserRole userRole) {

        // 고객이 아닌 사용자가 리뷰를 생성하려고 하면 예외 처리
        if (!userRole.equals(UserRole.USER)) {
            throw new ApplicationException(ErrorCode.INVALID_ROLE_FOR_REVIEW_CREATION);
        }

        Orders order = orderRepository.findById(reviewRequestDTO.getOrderId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // 주문의 고객이 현재 사용자가 아닌 경우 예외 처리
        if (!order.getCustomer().getId().equals(userId)) {
            throw new ApplicationException(ErrorCode.INVALID_USER_FOR_ORDER);
        }

        // 이미 리뷰가 작성된 경우 예외 처리
        if (reviewRepository.existsByOrderId(reviewRequestDTO.getOrderId())) {
            throw new ApplicationException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 주문이 완료되지 않은 경우 리뷰 작성 불가
        if (!order.getStatus().equals(OrderStatus.ORDER_DELIVERED)) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        // 리뷰 생성
        Review review = new Review();
        review.setOrder(order);
        review.setStore(order.getStore());
        review.setRating(reviewRequestDTO.getRating());
        review.setContent(reviewRequestDTO.getContent());
        review.setCreatedDate(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return convertToDTO(savedReview);
    }

    // 가게별 리뷰 조회 로직
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByStore(Long storeId, Integer ratingMin, Integer ratingMax) {
        List<Review> reviews = reviewRepository.findByStoreIdAndRatingRange(storeId, ratingMin, ratingMax);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 유효성 검사 및 DTO 변환 로직 ================================================================================
    private ReviewResponseDTO convertToDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setCreatedDate(review.getCreatedDate());
        dto.setStoreName(review.getStore().getName());  // 가게 이름 포함
        return dto;
    }
}
