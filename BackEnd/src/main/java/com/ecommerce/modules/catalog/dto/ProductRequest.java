package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request tạo/sửa Product. Trường {@code thumbnailUrl} có thể được cung cấp theo 2 cách:
 * <ul>
 *   <li>{@code useFileUpload = false} (mặc định): client gửi URL ảnh dạng string ở {@code thumbnailUrl}.</li>
 *   <li>{@code useFileUpload = true}: client phải đính kèm file ảnh ở part "file" của multipart request,
 *       backend sẽ lưu file qua {@code FileStorageService} và sinh URL public để ghi vào DB.</li>
 * </ul>
 */
public record ProductRequest(
        @NotNull Long categoryId,
        Long brandId,
        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 220) String slug,
        @Size(max = 500) String shortDescription,
        String description,
        @NotNull @DecimalMin("0") BigDecimal basePrice,
        @DecimalMin("0") BigDecimal salePrice,
        @Size(max = 2048) String thumbnailUrl,
        Boolean useFileUpload,
        ProductStatus status,
        Boolean isFeatured
) {}
