package com.ecommerce.modules.user.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.user.dto.AddressRequest;
import com.ecommerce.modules.user.dto.AddressResponse;
import com.ecommerce.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.modules.user.dto.UserResponse;
import com.ecommerce.modules.user.service.AddressService;
import com.ecommerce.modules.user.service.UserService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
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
@RequestMapping("/v1/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Me", description = "Profile và địa chỉ của user hiện tại")
public class MeController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping
    public ApiResponse<UserResponse> me(@CurrentUser UserPrincipal me) {
        return ApiResponse.ok(userService.getProfile(me.getId()));
    }

    /**
     * Cập nhật profile. Endpoint nhận {@code multipart/form-data}:
     * <ul>
     *   <li>part <b>data</b> (application/json): {@link UpdateProfileRequest} – chứa các trường profile và URL avatar.</li>
     *   <li>part <b>file</b> (binary, optional): file ảnh avatar, dùng khi {@code data.useFileUpload = true}.</li>
     * </ul>
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserResponse> updateMe(@CurrentUser UserPrincipal me,
                                              @Valid @RequestPart("data") UpdateProfileRequest req,
                                              @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok("Cập nhật profile thành công", userService.updateProfile(me.getId(), req, file));
    }

    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> addresses(@CurrentUser UserPrincipal me) {
        return ApiResponse.ok(addressService.list(me.getId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@CurrentUser UserPrincipal me,
                                                                     @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(addressService.create(me.getId(), req)));
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(@CurrentUser UserPrincipal me, @PathVariable Long id,
                                                      @Valid @RequestBody AddressRequest req) {
        return ApiResponse.ok(addressService.update(me.getId(), id, req));
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@CurrentUser UserPrincipal me, @PathVariable Long id) {
        addressService.delete(me.getId(), id);
        return ApiResponse.ok("Đã xoá địa chỉ", null);
    }

    @PatchMapping("/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@CurrentUser UserPrincipal me, @PathVariable Long id) {
        return ApiResponse.ok(addressService.setDefault(me.getId(), id));
    }
}
