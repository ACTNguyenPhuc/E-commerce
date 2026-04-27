package com.ecommerce.modules.payment.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.payment.dto.CheckoutResponse;
import com.ecommerce.modules.payment.dto.PaymentResponse;
import com.ecommerce.modules.payment.service.PaymentService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Thanh toán đơn hàng")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout/{orderCode}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CheckoutResponse> checkout(@CurrentUser UserPrincipal me, @PathVariable String orderCode) {
        return ApiResponse.ok(paymentService.checkout(me.getId(), orderCode));
    }

    @GetMapping("/{orderCode}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PaymentResponse>> byOrder(@CurrentUser UserPrincipal me, @PathVariable String orderCode) {
        return ApiResponse.ok(paymentService.getByOrder(me.getId(), orderCode));
    }

    @PostMapping("/webhook/{provider}")
    public ApiResponse<Void> webhook(@PathVariable String provider, @RequestBody Map<String, Object> payload) {
        // MVP – production cần verify chữ ký theo từng provider.
        String orderCode = (String) payload.get("orderCode");
        String txn = (String) payload.getOrDefault("transactionCode", "TXN-MOCK");
        boolean success = Boolean.TRUE.equals(payload.getOrDefault("success", true));
        paymentService.handleGatewayCallback(orderCode, txn, success, payload.toString());
        return ApiResponse.ok("Webhook processed", null);
    }
}
