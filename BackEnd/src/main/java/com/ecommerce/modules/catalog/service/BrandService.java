package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.util.SlugUtil;
import com.ecommerce.infrastructure.storage.FileStorageService;
import com.ecommerce.modules.catalog.dto.BrandRequest;
import com.ecommerce.modules.catalog.dto.BrandResponse;
import com.ecommerce.modules.catalog.entity.Brand;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private static final String FOLDER = "brands";

    private final BrandRepository repo;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<BrandResponse> listActive() {
        return repo.findByStatusOrderByNameAsc(CommonStatus.active).stream().map(BrandResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listAll() {
        return repo.findAll().stream().map(BrandResponse::from).toList();
    }

    @Transactional
    public BrandResponse create(BrandRequest req, MultipartFile file) {
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug() : SlugUtil.toSlug(req.name());
        if (repo.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        String logoUrl = fileStorageService.resolveImageOrUrl(
                file, req.logoUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER);
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        Brand b = Brand.builder()
                .name(req.name())
                .slug(slug)
                .logoUrl(logoUrl)
                .useFileUpload(useFile)
                .description(req.description())
                .status(req.status() != null ? req.status() : CommonStatus.active)
                .build();
        return BrandResponse.from(repo.save(b));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest req, MultipartFile file) {
        Brand b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
        b.setName(req.name());
        if (req.slug() != null && !req.slug().isBlank()) b.setSlug(req.slug());
        if (req.useFileUpload() != null) b.setUseFileUpload(Boolean.TRUE.equals(req.useFileUpload()));
        b.setLogoUrl(fileStorageService.resolveImageOrUrl(
                file, req.logoUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER));
        b.setDescription(req.description());
        if (req.status() != null) b.setStatus(req.status());
        return BrandResponse.from(b);
    }

    @Transactional
    public void delete(Long id) {
        Brand b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
        repo.delete(b);
    }
}
