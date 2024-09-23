package com.sparta.outsourcing.domain.review.controller;


import com.sparta.outsourcing.domain.review.dto.ReviewRequestDTO;
import com.sparta.outsourcing.domain.review.dto.ReviewResponseDTO;
import com.sparta.outsourcing.domain.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 리뷰 생성 API
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        ReviewResponseDTO reviewResponseDTO = reviewService.createReview(reviewRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponseDTO);
    }

    // 리뷰 조회 API
    @GetMapping("/store/{storeId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax) {

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByStore(storeId, ratingMin, ratingMax);
        return ResponseEntity.ok(reviews);
    }
}
