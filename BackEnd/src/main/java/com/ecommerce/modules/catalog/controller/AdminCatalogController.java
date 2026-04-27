package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import com.ecommerce.modules.catalog.service.BrandService;
import com.ecommerce.modules.catalog.service.CategoryService;
import com.ecommerce.modules.catalog.service.AttributeService;
import com.ecommerce.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.math.BigDecimal;

/**
 * Các endpoint create/update của catalog đều nhận {@code multipart/form-data} với 2 part:
 * <ul>
 *   <li><b>data</b> (application/json, bắt buộc): JSON body của request DTO.</li>
 *   <li><b>file</b> (binary, tuỳ chọn): file ảnh, chỉ dùng khi {@code data.useFileUpload = true}.</li>
 * </ul>
 * Khi {@code useFileUpload = false} (mặc định), trường URL trong DTO được dùng trực tiếp.
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
@RequiredArgsConstructor
@Tag(name = "Catalog (Admin)", description = "Quản lý catalog – chỉ admin/staff")
public class AdminCatalogController {

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final AttributeService attributeService;
    private final ProductService productService;

    // ----- Category -----
    @PostMapping(value = "/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestPart("data") CategoryRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(categoryService.create(req, file)));
    }

    @PutMapping(value = "/categories/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestPart("data") CategoryRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(categoryService.update(id, req, file));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok("Đã xoá", null);
    }

    // ----- Brand -----
    @PostMapping(value = "/brands", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestPart("data") BrandRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(brandService.create(req, file)));
    }

    @PutMapping(value = "/brands/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestPart("data") BrandRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(brandService.update(id, req, file));
    }

    @DeleteMapping("/brands/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return ApiResponse.ok("Đã xoá", null);
    }

    // ----- Product -----
    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductResponse>> adminListProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(productService.adminSearch(status, categoryId, brandId, keyword, minPrice, maxPrice, pageable));
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestPart("data") ProductRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.create(req, file)));
    }

    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("data") ProductRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(productService.update(id, req, file));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok("Đã xoá", null);
    }

    @PostMapping(value = "/products/{id}/variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @PathVariable Long id,
            @Valid @RequestPart("data") VariantRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.addVariant(id, req, file)));
    }

    @DeleteMapping("/products/{id}/variants/{variantId}")
    public ApiResponse<Void> deleteVariant(@PathVariable Long id, @PathVariable Long variantId) {
        productService.deleteVariant(id, variantId);
        return ApiResponse.ok("Đã xoá variant", null);
    }

    @PutMapping(value = "/products/{id}/variants/{variantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VariantResponse> updateVariant(
            @PathVariable Long id,
            @PathVariable Long variantId,
            @Valid @RequestPart("data") VariantRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(productService.updateVariant(id, variantId, req, file));
    }

    // ----- Product images -----
    @GetMapping("/products/{id}/images")
    public ApiResponse<List<ProductImageResponse>> listProductImages(@PathVariable Long id) {
        return ApiResponse.ok(productService.listImages(id));
    }

    @PostMapping(value = "/products/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductImageResponse>> addProductImage(
            @PathVariable Long id,
            @Valid @RequestPart("data") ProductImageRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info(req.toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.addImage(id, req, file)));
    }

    @DeleteMapping("/products/{id}/images/{imageId}")
    public ApiResponse<Void> deleteProductImage(@PathVariable Long id, @PathVariable Long imageId) {
        productService.deleteImage(id, imageId);
        return ApiResponse.ok("Đã xoá image", null);
    }

    @PutMapping("/products/{id}/images/{imageId}/primary")
    public ApiResponse<ProductImageResponse> setPrimaryProductImage(@PathVariable Long id, @PathVariable Long imageId) {
        return ApiResponse.ok(productService.setPrimaryImage(id, imageId));
    }

    // ----- Attributes -----
    @GetMapping("/attributes")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<ProductAttributeResponse>> listAttributes() {
        return ApiResponse.ok(attributeService.listAttributes());
    }

    @PostMapping("/attributes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductAttributeResponse>> createAttribute(@Valid @RequestBody ProductAttributeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(attributeService.createAttribute(req)));
    }

    @PutMapping("/attributes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductAttributeResponse> updateAttribute(@PathVariable Long id, @Valid @RequestBody ProductAttributeRequest req) {
        return ApiResponse.ok(attributeService.updateAttribute(id, req));
    }

    @DeleteMapping("/attributes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return ApiResponse.ok("Đã xoá", null);
    }

    @GetMapping("/attributes/{id}/values")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<AttributeValueResponse>> listAttributeValues(@PathVariable Long id) {
        return ApiResponse.ok(attributeService.listValues(id));
    }

    @GetMapping("/attribute-values")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<AttributeValueResponse>> listAllAttributeValues() {
        return ApiResponse.ok(attributeService.listAllValues());
    }

    @PostMapping("/attribute-values")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttributeValueResponse>> createAttributeValue(@Valid @RequestBody ProductAttributeValueRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(attributeService.createValue(req)));
    }

    @DeleteMapping("/attribute-values/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAttributeValue(@PathVariable Long id) {
        attributeService.deleteValue(id);
        return ApiResponse.ok("Đã xoá", null);
    }
}
