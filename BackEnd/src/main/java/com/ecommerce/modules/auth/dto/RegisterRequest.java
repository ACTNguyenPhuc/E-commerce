package com.ecommerce.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ") String phone,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 100) String fullName
) {}
