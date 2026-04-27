package com.ecommerce.modules.auth.dto;

import com.ecommerce.modules.user.dto.UserResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {}
