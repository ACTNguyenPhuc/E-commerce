package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.util.SlugUtil;
import com.ecommerce.infrastructure.storage.FileStorageService;
import com.ecommerce.modules.catalog.dto.CategoryRequest;
import com.ecommerce.modules.catalog.dto.CategoryResponse;
import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String FOLDER = "categories";

    private final CategoryRepository repo;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> tree() {
        List<Category> all = repo.findByStatusOrderByDisplayOrderAscIdAsc(CommonStatus.active);
        Map<Long, List<Category>> byParent = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(parent -> CategoryResponse.withChildren(parent,
                        byParent.getOrDefault(parent.getId(), List.of()).stream()
                                .map(CategoryResponse::from).toList()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        return CategoryResponse.from(repo.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category slug=" + slug)));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req, MultipartFile file) {
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug() : SlugUtil.toSlug(req.name());
        if (repo.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        String imageUrl = fileStorageService.resolveImageOrUrl(
                file, req.imageUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER);
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        Category c = Category.builder()
                .parentId(req.parentId())
                .name(req.name())
                .slug(slug)
                .description(req.description())
                .imageUrl(imageUrl)
                .useFileUpload(useFile)
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .status(req.status() != null ? req.status() : CommonStatus.active)
                .build();
        return CategoryResponse.from(repo.save(c));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req, MultipartFile file) {
        Category c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        c.setName(req.name());
        if (req.slug() != null && !req.slug().isBlank()) c.setSlug(req.slug());
        c.setParentId(req.parentId());
        c.setDescription(req.description());
        if (req.useFileUpload() != null) c.setUseFileUpload(Boolean.TRUE.equals(req.useFileUpload()));
        c.setImageUrl(fileStorageService.resolveImageOrUrl(
                file, req.imageUrl(), Boolean.TRUE.equals(req.useFileUpload()), FOLDER));
        if (req.displayOrder() != null) c.setDisplayOrder(req.displayOrder());
        if (req.status() != null) c.setStatus(req.status());
        return CategoryResponse.from(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        repo.delete(c);
    }
}
