package com.sparta.outsourcing.domain.review.repository;

import com.sparta.outsourcing.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //별점 필터링 & 정렬
    @Query("SELECT r FROM Review r WHERE r.store.id = :storeId AND " +
            "( :ratingMin IS NULL OR r.rating >= :ratingMin ) AND " +
            "( :ratingMax IS NULL OR r.rating <= :ratingMax ) " +
            "ORDER BY r.createdDate DESC")
    List<Review> findByStoreIdAndRatingRange(
            @Param("storeId") Long storeId,
            @Param("ratingMin") Integer ratingMin,
            @Param("ratingMax") Integer ratingMax
    );


    boolean existsByOrderId(Long orderId);}