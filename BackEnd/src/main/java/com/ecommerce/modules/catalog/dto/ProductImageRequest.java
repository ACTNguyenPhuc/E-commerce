package com.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.Size;

/**
 * Request tạo ảnh cho Product (product_images). Có thể cung cấp theo 2 cách:
 * - useFileUpload=false: client gửi URL string ở imageUrl
 * - useFileUpload=true: client đính kèm file ảnh ở part "file"
 */
public record ProductImageRequest(
        @Size(max = 2048) String imageUrl,
        Boolean useFileUpload,
        @Size(max = 200) String altText,
        Integer displayOrder,
        Boolean isPrimary
) {}

