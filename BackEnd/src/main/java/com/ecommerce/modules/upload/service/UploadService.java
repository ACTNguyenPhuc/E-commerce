package com.ecommerce.modules.upload.service;

import com.ecommerce.infrastructure.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class UploadService {

    private final FileStorageService fileStorageService;

    public UploadService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public String uploadImage(MultipartFile file, String folder) {
        String targetFolder = (folder == null || folder.isBlank()) ? "images" : folder;
        return fileStorageService.storeImage(file, targetFolder);
    }
}
