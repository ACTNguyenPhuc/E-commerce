package com.ecommerce.modules.voucher.dto;

import com.ecommerce.modules.voucher.entity.DiscountType;
import com.ecommerce.modules.voucher.entity.Voucher;
import com.ecommerce.modules.voucher.entity.VoucherStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record VoucherResponse(
        Long id,
        String code,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Integer usageLimitPerUser,
        Integer usedCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        VoucherStatus status
) {
    public static VoucherResponse from(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId()).code(v.getCode()).name(v.getName()).description(v.getDescription())
                .discountType(v.getDiscountType()).discountValue(v.getDiscountValue())
                .minOrderAmount(v.getMinOrderAmount()).maxDiscountAmount(v.getMaxDiscountAmount())
                .usageLimit(v.getUsageLimit()).usageLimitPerUser(v.getUsageLimitPerUser())
                .usedCount(v.getUsedCount())
                .startDate(v.getStartDate()).endDate(v.getEndDate())
                .status(v.getStatus())
                .build();
    }
}
