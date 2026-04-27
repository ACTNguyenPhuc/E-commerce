package com.ecommerce.modules.payment.dto;

import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.payment.entity.Payment;
import com.ecommerce.modules.payment.entity.PaymentTxnStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String transactionCode,
        PaymentTxnStatus status,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).orderId(p.getOrderId())
                .paymentMethod(p.getPaymentMethod()).amount(p.getAmount())
                .transactionCode(p.getTransactionCode()).status(p.getStatus())
                .paidAt(p.getPaidAt()).build();
    }
}
