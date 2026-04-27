package com.ecommerce.modules.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.order.dto.*;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.service.OrderService;
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
@RequestMapping("/v1/orders")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Orders (Customer)", description = "Đặt và quản lý đơn hàng của tôi")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/preview")
    public ApiResponse<OrderPreviewResponse> preview(@CurrentUser UserPrincipal me,
                                                     @Valid @RequestBody OrderPreviewRequest req) {
        return ApiResponse.ok(orderService.preview(me.getId(), req));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> place(@CurrentUser UserPrincipal me,
                                                                  @Valid @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(orderService.placeOrder(me.getId(), req)));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> myOrders(
            @CurrentUser UserPrincipal me,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.myOrders(me.getId(), status, pageable));
    }

    @GetMapping("/{code}")
    public ApiResponse<OrderDetailResponse> detail(@CurrentUser UserPrincipal me, @PathVariable String code) {
        return ApiResponse.ok(orderService.getOrderDetail(me.getId(), code, false));
    }

    @PostMapping("/{code}/cancel")
    public ApiResponse<OrderDetailResponse> cancel(@CurrentUser UserPrincipal me, @PathVariable String code) {
        return ApiResponse.ok("Đã huỷ đơn", orderService.cancelByCustomer(me.getId(), code));
    }
}
