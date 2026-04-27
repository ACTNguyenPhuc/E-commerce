package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record VariantResponse(
        Long id,
        Long productId,
        String sku,
        BigDecimal price,
        BigDecimal salePrice,
        Integer stockQuantity,
        String imageUrl,
        Boolean useFileUpload,
        BigDecimal weightGram,
        CommonStatus status,
        List<AttributeValueResponse> attributes
) {
    public static VariantResponse from(ProductVariant v, List<AttributeValueResponse> attrs) {
        return VariantResponse.builder()
                .id(v.getId())
                .productId(v.getProductId())
                .sku(v.getSku())
                .price(v.getPrice())
                .salePrice(v.getSalePrice())
                .stockQuantity(v.getStockQuantity())
                .imageUrl(v.getImageUrl())
                .useFileUpload(v.getUseFileUpload())
                .weightGram(v.getWeightGram())
                .status(v.getStatus())
                .attributes(attrs)
                .build();
    }
}
