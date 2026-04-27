package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductAttributeValue;
import lombok.Builder;

@Builder
public record AttributeValueResponse(
        Long id,
        Long attributeId,
        String attributeName,
        String value,
        String colorCode
) {
    public static AttributeValueResponse from(ProductAttributeValue v, String attributeName) {
        return AttributeValueResponse.builder()
                .id(v.getId())
                .attributeId(v.getAttributeId())
                .attributeName(attributeName)
                .value(v.getValue())
                .colorCode(v.getColorCode())
                .build();
    }
}
