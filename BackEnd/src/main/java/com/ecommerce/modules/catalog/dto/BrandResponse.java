package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Brand;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import lombok.Builder;

@Builder
public record BrandResponse(
        Long id,
        String name,
        String slug,
        String logoUrl,
        Boolean useFileUpload,
        String description,
        CommonStatus status
) {
    public static BrandResponse from(Brand b) {
        return BrandResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .slug(b.getSlug())
                .logoUrl(b.getLogoUrl())
                .useFileUpload(b.getUseFileUpload())
                .description(b.getDescription())
                .status(b.getStatus())
                .build();
    }
}
