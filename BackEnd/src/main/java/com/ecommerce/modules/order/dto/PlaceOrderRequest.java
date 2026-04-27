package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long addressId,
        @NotNull Long shippingMethodId,
        @NotNull PaymentMethod paymentMethod,
        String voucherCode,
        String note,
        @NotEmpty List<Item> items
) {
    public record Item(@NotNull Long productId, Long variantId, @NotNull @Min(1) Integer quantity) {}
}
