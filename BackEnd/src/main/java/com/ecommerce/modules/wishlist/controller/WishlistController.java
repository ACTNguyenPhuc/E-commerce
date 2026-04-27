package com.ecommerce.modules.wishlist.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.catalog.dto.ProductResponse;
import com.ecommerce.modules.wishlist.service.WishlistService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/wishlist")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Sản phẩm yêu thích")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> list(@CurrentUser UserPrincipal me) {
        return ApiResponse.ok(wishlistService.list(me.getId()));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Void> add(@CurrentUser UserPrincipal me, @PathVariable Long productId) {
        wishlistService.add(me.getId(), productId);
        return ApiResponse.ok("Đã thêm vào danh sách yêu thích", null);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@CurrentUser UserPrincipal me, @PathVariable Long productId) {
        wishlistService.remove(me.getId(), productId);
        return ApiResponse.ok("Đã xoá", null);
    }
}
