package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.service.BrandService;
import com.ecommerce.modules.catalog.service.CategoryService;
import com.ecommerce.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Catalog (Public)", description = "API public cho danh mục, thương hiệu, sản phẩm")
public class PublicCatalogController {

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(categoryService.tree());
    }

    @GetMapping("/categories/{slug}")
    public ApiResponse<CategoryResponse> categoryBySlug(@PathVariable String slug) {
        return ApiResponse.ok(categoryService.getBySlug(slug));
    }

    @GetMapping("/brands")
    public ApiResponse<List<BrandResponse>> brands() {
        return ApiResponse.ok(brandService.listActive());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductResponse>> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(productService.search(categoryId, brandId, keyword, minPrice, maxPrice, pageable));
    }

    @GetMapping("/products/featured")
    public ApiResponse<List<ProductResponse>> featured() {
        return ApiResponse.ok(productService.featured());
    }

    @GetMapping("/products/{slug}")
    public ApiResponse<ProductDetailResponse> productDetail(@PathVariable String slug) {
        return ApiResponse.ok(productService.detailBySlug(slug));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> search(
            @RequestParam(name = "q") String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(productService.search(null, null, keyword, null, null, pageable));
    }
}
