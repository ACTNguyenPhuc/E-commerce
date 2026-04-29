package com.ecommerce.modules.banner.dto;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Banner create/update. Field imageUrl supports 2 modes:
 * - useFileUpload=false: use imageUrl as string
 * - useFileUpload=true: attach file part "file"
 */
public record BannerRequest(
        @Size(max = 200) String title,
        @Size(max = 2048) String subtitle,
        @Size(max = 2048) String imageUrl,
        Boolean useFileUpload,
        @Size(max = 2048) String linkUrl,
        Integer displayOrder,
        @NotNull CommonStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}

