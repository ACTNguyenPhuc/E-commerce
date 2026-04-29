package com.ecommerce.modules.user.dto;

import com.ecommerce.modules.user.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 100) String recipientName,
        @NotBlank @Pattern(regexp = "^(0[0-9]{9})$") String recipientPhone,
        @NotBlank @Size(max = 100) String province,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String ward,
        @NotBlank @Size(max = 255) String streetAddress,
        AddressType addressType,
        Boolean isDefault
) {}
