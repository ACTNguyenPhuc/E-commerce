package com.ecommerce.modules.cart.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemResponse(
        Long id,
        Long productId,
        Long variantId,
        String productName,
        String variantName,
        String thumbnailUrl,
        Boolean useFileUpload,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        Integer stockQuantity
) {}
