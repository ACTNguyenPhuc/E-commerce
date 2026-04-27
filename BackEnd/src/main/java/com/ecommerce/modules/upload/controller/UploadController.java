package com.ecommerce.modules.upload.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.upload.service.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/v1/uploads")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload ảnh sản phẩm / avatar")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder
    ) {
        String url = uploadService.uploadImage(file, folder);
        return ApiResponse.ok("Upload thành công", Map.of("url", url));
    }
}
