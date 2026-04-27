package com.ecommerce.modules.user.dto;

import com.ecommerce.modules.user.entity.Gender;
import com.ecommerce.modules.user.entity.Role;
import com.ecommerce.modules.user.entity.User;
import com.ecommerce.modules.user.entity.UserStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String email,
        String phone,
        String fullName,
        String avatarUrl,
        Boolean useFileUpload,
        Gender gender,
        LocalDate dateOfBirth,
        Role role,
        UserStatus status,
        LocalDateTime emailVerifiedAt,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .phone(u.getPhone())
                .fullName(u.getFullName())
                .avatarUrl(u.getAvatarUrl())
                .useFileUpload(u.getUseFileUpload())
                .gender(u.getGender())
                .dateOfBirth(u.getDateOfBirth())
                .role(u.getRole())
                .status(u.getStatus())
                .emailVerifiedAt(u.getEmailVerifiedAt())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
