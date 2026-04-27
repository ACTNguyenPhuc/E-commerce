package com.ecommerce.modules.banner.dto;

import com.ecommerce.modules.banner.entity.Banner;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BannerResponse(
        Long id,
        String title,
        String subtitle,
        String imageUrl,
        Boolean useFileUpload,
        String linkUrl,
        Integer displayOrder,
        CommonStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BannerResponse from(Banner b) {
        return BannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .subtitle(b.getSubtitle())
                .imageUrl(b.getImageUrl())
                .useFileUpload(b.getUseFileUpload())
                .linkUrl(b.getLinkUrl())
                .displayOrder(b.getDisplayOrder())
                .status(b.getStatus())
                .startAt(b.getStartAt())
                .endAt(b.getEndAt())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

