package com.sparta.outsourcing.domain.review.service;

import com.sparta.outsourcing.domain.order.entity.Orders;
import com.sparta.outsourcing.domain.order.repository.OrderRepository;
import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.dto.ReviewResponseDTO;
import com.sparta.outsourcing.domain.review.entity.Review;
import com.sparta.outsourcing.domain.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    // 리뷰 생성 로직 (ReviewRequestDTO 사용)
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO) {
        Orders order = orderRepository.findById(reviewRequestDTO.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new IllegalStateException("배달 완료된 주문에만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByOrderId(reviewRequestDTO.getOrderId())) {
            throw new IllegalStateException("해당 주문에 이미 리뷰가 존재합니다.");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setStore(order.getStore());
        review.setRating(reviewRequestDTO.getRating());
        review.setContent(reviewRequestDTO.getContent());
        review.setCreatedDate(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return convertToDTO(savedReview);
    }


    public List<ReviewResponseDTO> getReviewsByStore(Long storeId, Integer ratingMin, Integer ratingMax) {
        List<Review> reviews = reviewRepository.findByStoreIdAndRatingRange(storeId, ratingMin, ratingMax);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


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

