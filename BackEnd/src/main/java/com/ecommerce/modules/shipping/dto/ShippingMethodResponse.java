package com.ecommerce.modules.shipping.dto;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.shipping.entity.ShippingMethod;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ShippingMethodResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal baseFee,
        Integer estimatedDaysMin,
        Integer estimatedDaysMax,
        CommonStatus status
) {
    public static ShippingMethodResponse from(ShippingMethod s) {
        return ShippingMethodResponse.builder()
                .id(s.getId()).code(s.getCode()).name(s.getName())
                .description(s.getDescription()).baseFee(s.getBaseFee())
                .estimatedDaysMin(s.getEstimatedDaysMin()).estimatedDaysMax(s.getEstimatedDaysMax())
                .status(s.getStatus()).build();
    }
}
