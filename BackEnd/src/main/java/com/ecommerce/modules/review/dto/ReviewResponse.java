package com.ecommerce.modules.review.dto;

import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(
        Long id,
        Long productId,
        Long userId,
        Integer rating,
        String title,
        String content,
        ReviewStatus status,
        Integer helpfulCount,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId()).productId(r.getProductId()).userId(r.getUserId())
                .rating(r.getRating()).title(r.getTitle()).content(r.getContent())
                .status(r.getStatus()).helpfulCount(r.getHelpfulCount())
                .createdAt(r.getCreatedAt()).build();
    }
}
