package com.ecommerce.modules.review.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.review.dto.ReviewRequest;
import com.ecommerce.modules.review.dto.ReviewResponse;
import com.ecommerce.modules.review.entity.ReviewStatus;
import com.ecommerce.modules.review.service.ReviewService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "Đánh giá sản phẩm")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/v1/products/{productId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> list(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(reviewService.listByProduct(productId, pageable));
    }

    @PostMapping("/v1/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@CurrentUser UserPrincipal me,
                                                              @PathVariable Long productId,
                                                              @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reviewService.create(me.getId(), productId, req)));
    }

    @PatchMapping("/v1/admin/reviews/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<ReviewResponse> moderate(@PathVariable Long id, @RequestParam ReviewStatus status) {
        return ApiResponse.ok(reviewService.moderate(id, status));
    }
}
