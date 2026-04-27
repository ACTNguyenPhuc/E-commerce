package com.ecommerce.modules.review.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.order.entity.OrderItem;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.repository.OrderItemRepository;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.review.dto.ReviewRequest;
import com.ecommerce.modules.review.dto.ReviewResponse;
import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewStatus;
import com.ecommerce.modules.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listByProduct(Long productId, Pageable pageable) {
        return PageResponse.from(reviewRepo.findByProductIdAndStatus(productId, ReviewStatus.approved, pageable),
                ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse create(Long userId, Long productId, ReviewRequest req) {
        if (req.orderItemId() != null) {
            OrderItem oi = orderItemRepo.findById(req.orderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem", req.orderItemId()));
            if (!oi.getProductId().equals(productId)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                        "OrderItem không khớp sản phẩm");
            }
            var order = orderRepo.findById(oi.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order " + oi.getOrderId()));
            if (!order.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Đơn không thuộc về bạn");
            }
            if (order.getStatus() != OrderStatus.delivered && order.getStatus() != OrderStatus.completed) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, HttpStatus.UNPROCESSABLE_ENTITY,
                        "Chỉ được review sau khi đơn đã giao");
            }
            if (reviewRepo.existsByUserIdAndOrderItemId(userId, req.orderItemId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT,
                        "Bạn đã đánh giá sản phẩm này trong đơn này");
            }
        }
        Review r = Review.builder()
                .productId(productId).userId(userId).orderItemId(req.orderItemId())
                .rating(req.rating()).title(req.title()).content(req.content())
                .status(ReviewStatus.pending)
                .build();
        return ReviewResponse.from(reviewRepo.save(r));
    }

    @Transactional
    public ReviewResponse moderate(Long reviewId, ReviewStatus status) {
        Review r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        r.setStatus(status);
        return ReviewResponse.from(r);
    }
}
