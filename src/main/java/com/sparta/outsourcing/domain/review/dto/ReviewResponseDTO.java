package com.sparta.outsourcing.domain.review.dto;

import java.time.LocalDateTime;

public class ReviewResponseDTO {

    private Long id;
    private int rating;
    private String content;
    private LocalDateTime createdDate;
    private String storeName;  // 가게 이름 추가 (엔티티에 없더라도 응답에 포함 가능)


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
