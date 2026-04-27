package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductDetailResponse(
        Long id,
        Long categoryId,
        Long brandId,
        String sku,
        String name,
        String slug,
        String shortDescription,
        String description,
        BigDecimal basePrice,
        BigDecimal salePrice,
        String thumbnailUrl,
        Boolean useFileUpload,
        ProductStatus status,
        Boolean isFeatured,
        Long viewCount,
        Long soldCount,
        BigDecimal ratingAvg,
        Long ratingCount,
        List<ProductImageResponse> images,
        List<VariantResponse> variants
) {
    public static ProductDetailResponse from(Product p,
                                             List<ProductImageResponse> images,
                                             List<VariantResponse> variants) {
        return ProductDetailResponse.builder()
                .id(p.getId())
                .categoryId(p.getCategoryId())
                .brandId(p.getBrandId())
                .sku(p.getSku())
                .name(p.getName())
                .slug(p.getSlug())
                .shortDescription(p.getShortDescription())
                .description(p.getDescription())
                .basePrice(p.getBasePrice())
                .salePrice(p.getSalePrice())
                .thumbnailUrl(p.getThumbnailUrl())
                .useFileUpload(p.getUseFileUpload())
                .status(p.getStatus())
                .isFeatured(p.getIsFeatured())
                .viewCount(p.getViewCount())
                .soldCount(p.getSoldCount())
                .ratingAvg(p.getRatingAvg())
                .ratingCount(p.getRatingCount())
                .images(images)
                .variants(variants)
                .build();
    }
}
