package com.ecommerce.modules.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.order.dto.OrderDetailResponse;
import com.ecommerce.modules.order.dto.OrderResponse;
import com.ecommerce.modules.order.dto.UpdateOrderStatusRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
@RequiredArgsConstructor
@Tag(name = "Orders (Admin)", description = "Quản lý đơn hàng – admin/staff")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.adminOrders(status, pageable));
    }

    @GetMapping("/{code}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable String code) {
        return ApiResponse.ok(orderService.getOrderDetail(null, code, true));
    }

    @PatchMapping("/{code}/status")
    public ApiResponse<OrderDetailResponse> updateStatus(@CurrentUser UserPrincipal me,
                                                         @PathVariable String code,
                                                         @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ApiResponse.ok(orderService.updateStatus(me.getId(), code, req));
    }

    @PostMapping("/{code}/cancel")
    public ApiResponse<OrderDetailResponse> cancel(@CurrentUser UserPrincipal me,
                                                   @PathVariable String code,
                                                   @RequestParam(required = false) String note) {
        return ApiResponse.ok("Đã huỷ đơn", orderService.cancelByAdmin(me.getId(), code, note));
    }
}
