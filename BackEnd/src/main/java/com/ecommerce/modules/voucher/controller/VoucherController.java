package com.ecommerce.modules.voucher.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.voucher.dto.*;
import com.ecommerce.modules.voucher.service.VoucherService;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Voucher", description = "Quản lý mã giảm giá")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/v1/vouchers/active")
    public ApiResponse<List<VoucherResponse>> active() {
        return ApiResponse.ok(voucherService.activeVouchers());
    }

    @PostMapping("/v1/vouchers/validate")
    public ApiResponse<VoucherValidationResult> validate(@AuthenticationPrincipal UserPrincipal me,
                                                          @Valid @RequestBody ValidateVoucherRequest req) {
        Long userId = me != null ? me.getId() : null;
        return ApiResponse.ok(voucherService.validate(userId, req));
    }

    // -------- ADMIN --------
    @GetMapping("/v1/admin/vouchers")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<VoucherResponse>> listAll() {
        return ApiResponse.ok(voucherService.listAll());
    }

    @PostMapping("/v1/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(voucherService.create(req)));
    }

    @PutMapping("/v1/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> update(@PathVariable Long id, @Valid @RequestBody VoucherRequest req) {
        return ApiResponse.ok(voucherService.update(id, req));
    }

    @DeleteMapping("/v1/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ApiResponse.ok("Đã xoá", null);
    }
}
