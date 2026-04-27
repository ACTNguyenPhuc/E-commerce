package com.ecommerce.modules.review.repository;

import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    boolean existsByUserIdAndOrderItemId(Long userId, Long orderItemId);
}
