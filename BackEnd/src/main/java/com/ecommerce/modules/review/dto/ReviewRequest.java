package com.ecommerce.modules.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        Long orderItemId,
        @Size(max = 200) String title,
        String content
) {}
