package com.ecommerce.modules.auth.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.auth.dto.*;
import com.ecommerce.modules.auth.service.AuthService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng ký, đăng nhập, refresh token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản customer mới")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authService.register(req)));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập, trả về access + refresh token")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Đăng nhập thành công", authService.login(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cấp access token mới từ refresh token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ApiResponse.ok(authService.refresh(req));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đổi mật khẩu")
    public ApiResponse<Void> changePassword(@CurrentUser UserPrincipal me,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(me.getId(), req);
        return ApiResponse.ok("Đổi mật khẩu thành công", null);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đăng xuất (client-side: huỷ bỏ token cục bộ)")
    public ApiResponse<Void> logout() {
        // MVP: stateless JWT, client tự xoá token. Có thể nâng cấp blacklist Redis sau.
        return ApiResponse.ok("Đăng xuất thành công", null);
    }
}
