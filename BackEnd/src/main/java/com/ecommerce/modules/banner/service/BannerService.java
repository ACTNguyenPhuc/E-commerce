package com.ecommerce.modules.banner.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.infrastructure.storage.FileStorageService;
import com.ecommerce.modules.banner.dto.BannerRequest;
import com.ecommerce.modules.banner.dto.BannerResponse;
import com.ecommerce.modules.banner.entity.Banner;
import com.ecommerce.modules.banner.repository.BannerRepository;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final String FOLDER = "banners";

    private final BannerRepository repo;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<BannerResponse> listActive() {
        return repo.findByStatusOrderByDisplayOrderAscIdAsc(CommonStatus.active)
                .stream().map(BannerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> listAll() {
        return repo.findAll().stream().map(BannerResponse::from).toList();
    }

    @Transactional
    public BannerResponse create(BannerRequest req, MultipartFile file) {
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        String imageUrl = fileStorageService.resolveImageOrUrl(file, req.imageUrl(), useFile, FOLDER);
        Banner b = Banner.builder()
                .title(req.title())
                .subtitle(req.subtitle())
                .imageUrl(imageUrl)
                .useFileUpload(useFile)
                .linkUrl(req.linkUrl())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .status(req.status() != null ? req.status() : CommonStatus.active)
                .startAt(req.startAt())
                .endAt(req.endAt())
                .build();
        return BannerResponse.from(repo.save(b));
    }

    @Transactional
    public BannerResponse update(Long id, BannerRequest req, MultipartFile file) {
        Banner b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner", id));
        if (req.title() != null) b.setTitle(req.title());
        if (req.subtitle() != null) b.setSubtitle(req.subtitle());
        if (req.linkUrl() != null) b.setLinkUrl(req.linkUrl());
        if (req.displayOrder() != null) b.setDisplayOrder(req.displayOrder());
        if (req.status() != null) b.setStatus(req.status());
        b.setStartAt(req.startAt());
        b.setEndAt(req.endAt());

        if (req.useFileUpload() != null) b.setUseFileUpload(Boolean.TRUE.equals(req.useFileUpload()));
        // Update image only when useFileUpload=true or client provides imageUrl
        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        if (useFile || req.imageUrl() != null) {
            String imageUrl = fileStorageService.resolveImageOrUrl(file, req.imageUrl(), useFile, FOLDER);
            b.setImageUrl(imageUrl);
            b.setUseFileUpload(useFile);
        }
        return BannerResponse.from(b);
    }

    @Transactional
    public void delete(Long id) {
        Banner b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner", id));
        repo.delete(b);
    }
}

