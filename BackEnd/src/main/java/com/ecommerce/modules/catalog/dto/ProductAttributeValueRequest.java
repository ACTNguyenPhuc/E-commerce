package com.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductAttributeValueRequest(
        @NotNull Long attributeId,
        @NotBlank @Size(max = 100) String value,
        @Size(max = 20) String colorCode,
        Integer displayOrder
) {}

