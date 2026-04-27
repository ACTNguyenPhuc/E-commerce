package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductAttribute;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductAttributeResponse(
        Long id,
        String name,
        String slug,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductAttributeResponse from(ProductAttribute a) {
        return ProductAttributeResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .slug(a.getSlug())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

