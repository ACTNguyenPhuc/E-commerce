package com.ecommerce.modules.voucher.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValidateVoucherRequest(
        @NotBlank String code,
        @NotNull @DecimalMin("0") BigDecimal subtotal
) {}
