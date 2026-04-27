package com.ecommerce.modules.cart.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.cart.dto.AddCartItemRequest;
import com.ecommerce.modules.cart.dto.CartResponse;
import com.ecommerce.modules.cart.dto.UpdateCartItemRequest;
import com.ecommerce.modules.cart.service.CartService;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Giỏ hàng (hỗ trợ guest qua header X-Session-Id)")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> get(@AuthenticationPrincipal UserPrincipal me,
                                         @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok(cartService.getOrCreate(userId(me), sessionId));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> add(@AuthenticationPrincipal UserPrincipal me,
                                         @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                         @Valid @RequestBody AddCartItemRequest req) {
        return ApiResponse.ok("Đã thêm vào giỏ", cartService.addItem(userId(me), sessionId, req));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> update(@AuthenticationPrincipal UserPrincipal me,
                                            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                            @PathVariable Long itemId,
                                            @Valid @RequestBody UpdateCartItemRequest req) {
        return ApiResponse.ok(cartService.updateItem(userId(me), sessionId, itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> remove(@AuthenticationPrincipal UserPrincipal me,
                                            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                            @PathVariable Long itemId) {
        return ApiResponse.ok(cartService.removeItem(userId(me), sessionId, itemId));
    }

    @DeleteMapping
    public ApiResponse<Void> clear(@AuthenticationPrincipal UserPrincipal me,
                                   @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        cartService.clear(userId(me), sessionId);
        return ApiResponse.ok("Đã xoá giỏ", null);
    }

    @PostMapping("/merge")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartResponse> merge(@AuthenticationPrincipal UserPrincipal me,
                                           @RequestHeader("X-Session-Id") String guestSessionId) {
        return ApiResponse.ok(cartService.merge(me.getId(), guestSessionId));
    }

    private Long userId(UserPrincipal me) {
        return me != null ? me.getId() : null;
    }
}
