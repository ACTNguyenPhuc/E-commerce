package com.ecommerce.modules.cart.dto;

import lombok.Builder;
import com.ecommerce.modules.catalog.dto.AttributeValueResponse;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartItemResponse(
        Long id,
        Long productId,
        Long variantId,
        String productName,
        String variantName,
        List<AttributeValueResponse> attributes,
        String thumbnailUrl,
        Boolean useFileUpload,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        Integer stockQuantity
) {}
