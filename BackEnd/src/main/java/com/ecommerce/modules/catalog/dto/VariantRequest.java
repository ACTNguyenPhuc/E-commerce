package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request tạo Variant. Trường {@code imageUrl} có thể được cung cấp theo 2 cách:
 * <ul>
 *   <li>{@code useFileUpload = false} (mặc định): client gửi URL ảnh dạng string ở {@code imageUrl}.</li>
 *   <li>{@code useFileUpload = true}: client phải đính kèm file ảnh ở part "file" của multipart request,
 *       backend sẽ lưu file qua {@code FileStorageService} và sinh URL public để ghi vào DB.</li>
 * </ul>
 */
public record VariantRequest(
        @NotBlank @Size(max = 100) String sku,
        @NotNull @DecimalMin("0") BigDecimal price,
        @DecimalMin("0") BigDecimal salePrice,
        @NotNull @Min(0) Integer stockQuantity,
        @Size(max = 2048) String imageUrl,
        Boolean useFileUpload,
        BigDecimal weightGram,
        CommonStatus status,
        List<Long> attributeValueIds
) {}
