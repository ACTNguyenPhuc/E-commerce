package com.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 120) String slug
) {}

