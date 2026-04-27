package com.ecommerce.modules.cart.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(
        Long cartId,
        Long userId,
        String sessionId,
        List<CartItemResponse> items,
        Integer totalQuantity,
        BigDecimal subtotal
) {}
