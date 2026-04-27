package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductImage;
import lombok.Builder;

@Builder
public record ProductImageResponse(
        Long id,
        String imageUrl,
        Boolean useFileUpload,
        String altText,
        Integer displayOrder,
        Boolean isPrimary
) {
    public static ProductImageResponse from(ProductImage i) {
        return ProductImageResponse.builder()
                .id(i.getId())
                .imageUrl(i.getImageUrl())
                .useFileUpload(i.getUseFileUpload())
                .altText(i.getAltText())
                .displayOrder(i.getDisplayOrder())
                .isPrimary(i.getIsPrimary())
                .build();
    }
}
