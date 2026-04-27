package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDetailResponse(
        Long id,
        String orderCode,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus status,
        String note,
        LocalDateTime placedAt,
        LocalDateTime confirmedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        List<Item> items,
        List<History> history
) {
    @Builder
    public record Item(Long id, Long productId, Long variantId, String productName,
                       String variantName, String sku, BigDecimal unitPrice,
                       Integer quantity, BigDecimal subtotal) {
        public static Item from(OrderItem oi) {
            return Item.builder()
                    .id(oi.getId()).productId(oi.getProductId()).variantId(oi.getVariantId())
                    .productName(oi.getProductName()).variantName(oi.getVariantName())
                    .sku(oi.getSku()).unitPrice(oi.getUnitPrice())
                    .quantity(oi.getQuantity()).subtotal(oi.getSubtotal())
                    .build();
        }
    }

    @Builder
    public record History(String fromStatus, String toStatus, String note, LocalDateTime createdAt) {
        public static History from(OrderStatusHistory h) {
            return History.builder()
                    .fromStatus(h.getFromStatus()).toStatus(h.getToStatus())
                    .note(h.getNote()).createdAt(h.getCreatedAt()).build();
        }
    }
}
