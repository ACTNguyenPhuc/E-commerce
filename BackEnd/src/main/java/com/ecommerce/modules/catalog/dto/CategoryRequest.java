package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request tạo/sửa Category. Trường {@code imageUrl} có thể được cung cấp theo 2 cách:
 * <ul>
 *   <li>{@code useFileUpload = false} (mặc định): client gửi URL ảnh dạng string ở {@code imageUrl}.</li>
 *   <li>{@code useFileUpload = true}: client phải đính kèm file ảnh ở part "file" của multipart request,
 *       backend sẽ lưu file qua {@code FileStorageService} và sinh URL public để ghi vào DB.</li>
 * </ul>
 */
public record CategoryRequest(
        @NotBlank @Size(max = 150) String name,
        Long parentId,
        @Size(max = 180) String slug,
        String description,
        @Size(max = 2048) String imageUrl,
        Boolean useFileUpload,
        Integer displayOrder,
        CommonStatus status
) {}
