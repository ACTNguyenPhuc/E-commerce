package com.ecommerce.modules.banner.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.banner.dto.BannerRequest;
import com.ecommerce.modules.banner.dto.BannerResponse;
import com.ecommerce.modules.banner.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Banners", description = "Banner endpoints")
public class BannerController {

    private final BannerService bannerService;

    // Public
    @GetMapping("/v1/banners")
    public ApiResponse<List<BannerResponse>> listActive() {
        return ApiResponse.ok(bannerService.listActive());
    }

    // Admin
    @GetMapping("/v1/admin/banners")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<BannerResponse>> listAll() {
        return ApiResponse.ok(bannerService.listAll());
    }

    @PostMapping(value = "/v1/admin/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> create(
            @Valid @RequestPart("data") BannerRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(bannerService.create(req, file)));
    }

    @PutMapping(value = "/v1/admin/banners/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") BannerRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(bannerService.update(id, req, file));
    }

    @DeleteMapping("/v1/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ApiResponse.ok("Đã xoá", null);
    }
}

