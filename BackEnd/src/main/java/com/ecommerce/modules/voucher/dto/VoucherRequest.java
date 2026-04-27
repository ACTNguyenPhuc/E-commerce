package com.ecommerce.modules.voucher.dto;

import com.ecommerce.modules.voucher.entity.DiscountType;
import com.ecommerce.modules.voucher.entity.VoucherStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0") BigDecimal discountValue,
        @DecimalMin("0") BigDecimal minOrderAmount,
        @DecimalMin("0") BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Integer usageLimitPerUser,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        VoucherStatus status
) {}
