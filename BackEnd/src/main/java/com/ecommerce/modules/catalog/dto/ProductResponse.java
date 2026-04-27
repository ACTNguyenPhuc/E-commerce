package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        Long categoryId,
        Long brandId,
        String sku,
        String name,
        String slug,
        String shortDescription,
        BigDecimal basePrice,
        BigDecimal salePrice,
        String thumbnailUrl,
        Boolean useFileUpload,
        ProductStatus status,
        Boolean isFeatured,
        Long soldCount,
        BigDecimal ratingAvg,
        Long ratingCount
) {
    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .categoryId(p.getCategoryId())
                .brandId(p.getBrandId())
                .sku(p.getSku())
                .name(p.getName())
                .slug(p.getSlug())
                .shortDescription(p.getShortDescription())
                .basePrice(p.getBasePrice())
                .salePrice(p.getSalePrice())
                .thumbnailUrl(p.getThumbnailUrl())
                .useFileUpload(p.getUseFileUpload())
                .status(p.getStatus())
                .isFeatured(p.getIsFeatured())
                .soldCount(p.getSoldCount())
                .ratingAvg(p.getRatingAvg())
                .ratingCount(p.getRatingCount())
                .build();
    }
}
