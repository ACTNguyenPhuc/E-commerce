package com.ecommerce.modules.upload.controller;

import com.ecommerce.infrastructure.storage.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Tải file theo URL public")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Resource> getByUrl(@RequestParam("url") String url) throws IOException {
        Resource resource = fileStorageService.loadByPublicUrl(url);

        String contentType = null;
        try {
            if (resource.isFile()) {
                contentType = Files.probeContentType(resource.getFile().toPath());
            }
        } catch (Exception ignored) {
            contentType = null;
        }
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .contentType(mediaType)
                .body(resource);
    }
}

