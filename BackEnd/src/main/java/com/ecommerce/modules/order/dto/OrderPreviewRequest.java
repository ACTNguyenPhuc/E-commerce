package com.ecommerce.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderPreviewRequest(
        @NotNull Long shippingMethodId,
        String voucherCode,
        @NotEmpty List<Item> items
) {
    public record Item(@NotNull Long productId, Long variantId, @NotNull @Min(1) Integer quantity) {}
}
