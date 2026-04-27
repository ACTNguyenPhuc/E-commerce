package com.ecommerce.modules.payment.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.payment.dto.CheckoutResponse;
import com.ecommerce.modules.payment.dto.PaymentResponse;
import com.ecommerce.modules.payment.entity.Payment;
import com.ecommerce.modules.payment.entity.PaymentTxnStatus;
import com.ecommerce.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;

    @Value("${app.payment.vnpay.pay-url}")
    private String vnpayUrl;

    @Value("${app.payment.vnpay.return-url}")
    private String vnpayReturnUrl;

    @Transactional
    public CheckoutResponse checkout(Long userId, String orderCode) {
        Order order = orderRepo.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        if (order.getPaymentStatus() == PaymentStatus.paid) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT, HttpStatus.UNPROCESSABLE_ENTITY, "Đơn đã thanh toán");
        }
        // Tuỳ payment_method, sinh URL khác nhau. MVP: VNPay redirect URL stub.
        String url = switch (order.getPaymentMethod()) {
            case vnpay   -> buildVnpayUrl(order);
            case momo    -> "https://test-payment.momo.vn/?order=" + order.getOrderCode();
            case zalopay -> "https://sb-openapi.zalopay.vn/?order=" + order.getOrderCode();
            case credit_card -> "https://stripe.example.com/checkout?order=" + order.getOrderCode();
            case cod, bank_transfer -> null;
        };
        return CheckoutResponse.builder()
                .orderCode(order.getOrderCode())
                .paymentUrl(url)
                .message(url == null ? "Phương thức không cần redirect" : "Mở URL để thanh toán")
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByOrder(Long userId, String orderCode) {
        Order o = orderRepo.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        return paymentRepo.findByOrderIdOrderByCreatedAtDesc(o.getId())
                .stream().map(PaymentResponse::from).toList();
    }

    /** Webhook xử lý kết quả thanh toán từ gateway. MVP: nhận trực tiếp transactionCode + status. */
    @Transactional
    public void handleGatewayCallback(String orderCode, String transactionCode, boolean success, String rawResponse) {
        Order o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        Payment p = paymentRepo.findByOrderIdOrderByCreatedAtDesc(o.getId()).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order " + orderCode));
        p.setTransactionCode(transactionCode);
        p.setGatewayResponse(rawResponse);
        if (success) {
            p.setStatus(PaymentTxnStatus.success);
            p.setPaidAt(LocalDateTime.now());
            o.setPaymentStatus(PaymentStatus.paid);
            if (o.getStatus() == OrderStatus.pending) {
                o.setStatus(OrderStatus.confirmed);
                o.setConfirmedAt(LocalDateTime.now());
            }
        } else {
            p.setStatus(PaymentTxnStatus.failed);
            o.setPaymentStatus(PaymentStatus.failed);
        }
    }

    private String buildVnpayUrl(Order order) {
        // MVP stub – production cần HMAC-SHA512, tham số chuẩn VNPay
        String txnRef = order.getOrderCode() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return vnpayUrl + "?vnp_TmnCode=DEMO"
                + "&vnp_Amount=" + order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).toBigInteger()
                + "&vnp_TxnRef=" + URLEncoder.encode(txnRef, StandardCharsets.UTF_8)
                + "&vnp_OrderInfo=" + URLEncoder.encode("Thanh toan don " + order.getOrderCode(), StandardCharsets.UTF_8)
                + "&vnp_ReturnUrl=" + URLEncoder.encode(vnpayReturnUrl, StandardCharsets.UTF_8);
    }
}
