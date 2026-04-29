package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.common.util.SlugUtil;
import com.ecommerce.common.util.HtmlSanitizer;
import com.ecommerce.infrastructure.storage.FileStorageService;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.entity.*;
import com.ecommerce.modules.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String FOLDER_PRODUCT = "products";
    private static final String FOLDER_VARIANT = "variants";

    private final ProductRepository productRepo;
    private final CategoryService categoryService;
    private final ProductImageRepository imageRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductVariantValueRepository pvvRepo;
    private final ProductAttributeRepository attrRepo;
    private final ProductAttributeValueRepository attrValueRepo;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(Long categoryId, Long brandId, String keyword,
                                                BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        List<Long> categoryIds = categoryId != null ? categoryService.collectDescendantIds(categoryId) : null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        Page<Product> page = productRepo.searchByCategoryIds(ProductStatus.active, categoryIds, brandId, keyword, minPrice, maxPrice, pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> adminSearch(ProductStatus status,
                                                     Long categoryId, Long brandId, String keyword,
                                                     BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        List<Long> categoryIds = categoryId != null ? categoryService.collectDescendantIds(categoryId) : null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        Page<Product> page = productRepo.adminSearchByCategoryIds(status, categoryIds, brandId, keyword, minPrice, maxPrice, pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> featured() {
        return productRepo.findTop10ByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(ProductStatus.active)
                .stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductDetailResponse detailBySlug(String slug) {
        Product p = productRepo.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product slug=" + slug));
        productRepo.incrementViewCount(p.getId());
        return buildDetail(p);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse detailById(Long id) {
        Product p = productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return buildDetail(p);
    }

    private ProductDetailResponse buildDetail(Product p) {
        List<ProductImageResponse> images = imageRepo.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId())
                .stream().map(ProductImageResponse::from).toList();

        List<ProductVariant> variants = variantRepo.findByProductId(p.getId());
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        List<ProductVariantValue> pvvs = variantIds.isEmpty() ? List.of()
                : pvvRepo.findAll().stream().filter(v -> variantIds.contains(v.getVariantId())).toList();

        List<Long> avIds = pvvs.stream().map(ProductVariantValue::getAttributeValueId).distinct().toList();
        Map<Long, ProductAttributeValue> avMap = avIds.isEmpty() ? Map.of()
                : attrValueRepo.findByIdIn(avIds).stream()
                .collect(Collectors.toMap(ProductAttributeValue::getId, x -> x));

        Map<Long, String> attrNameMap = attrRepo.findAll().stream()
                .collect(Collectors.toMap(ProductAttribute::getId, ProductAttribute::getName));

        List<VariantResponse> variantResponses = variants.stream().map(v -> {
            List<AttributeValueResponse> attrs = pvvs.stream()
                    .filter(pv -> pv.getVariantId().equals(v.getId()))
                    .map(pv -> avMap.get(pv.getAttributeValueId()))
                    .filter(av -> av != null)
                    .map(av -> AttributeValueResponse.from(av, attrNameMap.get(av.getAttributeId())))
                    .toList();
            return VariantResponse.from(v, attrs);
        }).toList();

        return ProductDetailResponse.from(p, images, variantResponses);
    }

    @Transactional
    public ProductDetailResponse create(ProductRequest req, MultipartFile file) {
        if (productRepo.existsBySku(req.sku())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "SKU đã tồn tại");
        }
        String slug = (req.slug() != null && !req.slug().isBlank()) ? req.slug() : SlugUtil.toSlug(req.name());
        if (productRepo.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        String thumbnailUrl = fileStorageService.resolveImageOrUrl(
                file, req.thumbnailUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER_PRODUCT);
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        Product p = Product.builder()
                .categoryId(req.categoryId()).brandId(req.brandId())
                .sku(req.sku()).name(req.name()).slug(slug)
                .shortDescription(req.shortDescription()).description(HtmlSanitizer.sanitize(req.description()))
                .basePrice(req.basePrice()).salePrice(req.salePrice())
                .thumbnailUrl(thumbnailUrl)
                .useFileUpload(useFile)
                .status(req.status() != null ? req.status() : ProductStatus.active)
                .isFeatured(Boolean.TRUE.equals(req.isFeatured()))
                .build();
        p = productRepo.save(p);
        return buildDetail(p);
    }

    @Transactional
    public ProductDetailResponse update(Long id, ProductRequest req, MultipartFile file) {
        Product p = productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
        p.setCategoryId(req.categoryId());
        p.setBrandId(req.brandId());
        p.setName(req.name());
        if (req.slug() != null && !req.slug().isBlank()) p.setSlug(req.slug());
        p.setShortDescription(req.shortDescription());
        log.info(req.description());
        log.info("Description"+HtmlSanitizer.sanitize((req.description())));

        p.setDescription(HtmlSanitizer.sanitize(req.description()));
        p.setBasePrice(req.basePrice());
        p.setSalePrice(req.salePrice());
        if (req.useFileUpload() != null) p.setUseFileUpload(Boolean.TRUE.equals(req.useFileUpload()));
        p.setThumbnailUrl(fileStorageService.resolveImageOrUrl(
                file, req.thumbnailUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER_PRODUCT));
        if (req.status() != null) p.setStatus(req.status());
        if (req.isFeatured() != null) p.setIsFeatured(req.isFeatured());
        return buildDetail(p);
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productRepo.delete(p);
    }

    @Transactional
    public VariantResponse addVariant(Long productId, VariantRequest req, MultipartFile file) {
        productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (variantRepo.existsBySku(req.sku())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "SKU variant đã tồn tại");
        }
        String imageUrl = fileStorageService.resolveImageOrUrl(
                file, req.imageUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER_VARIANT);
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        ProductVariant v = ProductVariant.builder()
                .productId(productId).sku(req.sku()).price(req.price()).salePrice(req.salePrice())
                .stockQuantity(req.stockQuantity()).imageUrl(imageUrl).useFileUpload(useFile).weightGram(req.weightGram())
                .status(req.status() != null ? req.status() : CommonStatus.active)
                .build();
        v = variantRepo.save(v);
        if (req.attributeValueIds() != null) {
            for (Long avId : req.attributeValueIds()) {
                pvvRepo.save(ProductVariantValue.builder().variantId(v.getId()).attributeValueId(avId).build());
            }
        }
        return loadVariantResponse(v);
    }

    @Transactional(readOnly = true)
    public java.util.List<ProductImageResponse> listImages(Long productId) {
        productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return imageRepo.findByProductIdOrderByDisplayOrderAscIdAsc(productId)
                .stream().map(ProductImageResponse::from).toList();
    }

    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageRequest req, MultipartFile file) {
        productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        String imageUrl = fileStorageService.resolveImageOrUrl(file, req.imageUrl(), useFile, FOLDER_PRODUCT);
        ProductImage img = ProductImage.builder()
                .productId(productId)
                .imageUrl(imageUrl)
                .useFileUpload(useFile)
                .altText(req.altText())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .isPrimary(Boolean.TRUE.equals(req.isPrimary()))
                .build();
        if (Boolean.TRUE.equals(req.isPrimary())) {
            imageRepo.unsetPrimaryForProduct(productId);
        }
        return ProductImageResponse.from(imageRepo.save(img));
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        if (!img.getProductId().equals(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Image không thuộc product này");
        }
        imageRepo.delete(img);
    }

    @Transactional
    public ProductImageResponse setPrimaryImage(Long productId, Long imageId) {
        ProductImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        if (!img.getProductId().equals(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Image không thuộc product này");
        }
        imageRepo.unsetPrimaryForProduct(productId);
        img.setIsPrimary(true);
        return ProductImageResponse.from(img);
    }

    @Transactional
    public VariantResponse updateVariant(Long productId, Long variantId, VariantRequest req, MultipartFile file) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
        if (!v.getProductId().equals(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Variant không thuộc product này");
        }

        v.setSku(req.sku());
        v.setPrice(req.price());
        v.setSalePrice(req.salePrice());
        v.setStockQuantity(req.stockQuantity());
        v.setWeightGram(req.weightGram());
        if (req.status() != null) v.setStatus(req.status());

        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        if (useFile || req.imageUrl() != null) {
            String imageUrl = fileStorageService.resolveImageOrUrl(
                    file, req.imageUrl(), useFile, FOLDER_VARIANT);
            v.setImageUrl(imageUrl);
            v.setUseFileUpload(useFile);
        }

        // Update variant attribute values (replace all)
        pvvRepo.deleteByVariantId(v.getId());
        if (req.attributeValueIds() != null) {
            for (Long avId : req.attributeValueIds()) {
                pvvRepo.save(ProductVariantValue.builder().variantId(v.getId()).attributeValueId(avId).build());
            }
        }

        return loadVariantResponse(v);
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
        if (!v.getProductId().equals(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Variant không thuộc product này");
        }
        variantRepo.delete(v);
    }

    private VariantResponse loadVariantResponse(ProductVariant v) {
        List<ProductVariantValue> pvvs = pvvRepo.findByVariantId(v.getId());
        List<Long> avIds = pvvs.stream().map(ProductVariantValue::getAttributeValueId).toList();
        Map<Long, ProductAttributeValue> avMap = avIds.isEmpty() ? Map.of()
                : attrValueRepo.findByIdIn(avIds).stream().collect(Collectors.toMap(ProductAttributeValue::getId, x -> x));
        Map<Long, String> attrNameMap = attrRepo.findAll().stream()
                .collect(Collectors.toMap(ProductAttribute::getId, ProductAttribute::getName));
        List<AttributeValueResponse> attrs = avIds.stream()
                .map(avMap::get).filter(av -> av != null)
                .map(av -> AttributeValueResponse.from(av, attrNameMap.get(av.getAttributeId())))
                .toList();
        return VariantResponse.from(v, attrs);
    }
}
