package com.ecommerce.modules.payment.dto;

import lombok.Builder;

@Builder
public record CheckoutResponse(
        String orderCode,
        String paymentUrl,
        String message
) {}
