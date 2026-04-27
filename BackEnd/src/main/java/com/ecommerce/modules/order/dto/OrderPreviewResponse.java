package com.ecommerce.modules.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderPreviewResponse(
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String voucherCode,
        List<LineItem> items
) {
    @Builder
    public record LineItem(
            Long productId, Long variantId, String productName, String variantName,
            String sku, BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {}
}
