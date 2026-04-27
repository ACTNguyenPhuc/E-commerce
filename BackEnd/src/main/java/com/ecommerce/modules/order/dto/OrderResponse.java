package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderResponse(
        Long id,
        String orderCode,
        Long userId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus status,
        LocalDateTime placedAt,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId()).orderCode(o.getOrderCode()).userId(o.getUserId())
                .subtotal(o.getSubtotal()).shippingFee(o.getShippingFee())
                .discountAmount(o.getDiscountAmount()).totalAmount(o.getTotalAmount())
                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .placedAt(o.getPlacedAt()).createdAt(o.getCreatedAt())
                .build();
    }
}
