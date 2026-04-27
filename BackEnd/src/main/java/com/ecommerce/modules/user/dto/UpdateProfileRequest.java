package com.ecommerce.modules.user.dto;

import com.ecommerce.modules.user.entity.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request cập nhật profile. Trường {@code avatarUrl} có thể được cung cấp theo 2 cách:
 * <ul>
 *   <li>{@code useFileUpload = false} (mặc định): client gửi URL ảnh dạng string ở {@code avatarUrl}.</li>
 *   <li>{@code useFileUpload = true}: client phải đính kèm file ảnh ở part "file" của multipart request,
 *       backend sẽ lưu file qua {@code FileStorageService} và sinh URL public để ghi vào DB.</li>
 * </ul>
 */
public record UpdateProfileRequest(
        @Size(max = 100) String fullName,
        @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ") String phone,
        Gender gender,
        LocalDate dateOfBirth,
        @Size(max = 2048) String avatarUrl,
        Boolean useFileUpload
) {}
