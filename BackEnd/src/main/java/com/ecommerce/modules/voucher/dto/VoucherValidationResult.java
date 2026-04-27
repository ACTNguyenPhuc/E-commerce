package com.ecommerce.modules.voucher.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VoucherValidationResult(
        boolean valid,
        String code,
        BigDecimal discountAmount,
        String message
) {}
