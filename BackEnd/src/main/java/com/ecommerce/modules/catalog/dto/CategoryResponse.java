package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryResponse(
        Long id,
        Long parentId,
        String name,
        String slug,
        String description,
        String imageUrl,
        Boolean useFileUpload,
        Integer displayOrder,
        CommonStatus status,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .useFileUpload(c.getUseFileUpload())
                .displayOrder(c.getDisplayOrder())
                .status(c.getStatus())
                .build();
    }

    public static CategoryResponse withChildren(Category c, List<CategoryResponse> children) {
        return CategoryResponse.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .useFileUpload(c.getUseFileUpload())
                .displayOrder(c.getDisplayOrder())
                .status(c.getStatus())
                .children(children)
                .build();
    }
}
