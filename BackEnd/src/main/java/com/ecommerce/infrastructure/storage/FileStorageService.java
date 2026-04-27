package com.ecommerce.infrastructure.storage;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> DEFAULT_ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path baseDir;
    private final String publicUrlPrefix;

    public FileStorageService(
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.public-url}") String publicUrlPrefix
    ) {
        if (!StringUtils.hasText(uploadDir)) {
            throw new IllegalStateException("app.upload.dir is required");
        }
        if (!StringUtils.hasText(publicUrlPrefix)) {
            throw new IllegalStateException("app.upload.public-url is required");
        }
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicUrlPrefix = publicUrlPrefix.endsWith("/")
                ? publicUrlPrefix.substring(0, publicUrlPrefix.length() - 1)
                : publicUrlPrefix;
    }

    public String storeImage(MultipartFile file, String folder) {
        return store(file, folder, DEFAULT_ALLOWED_IMAGE_TYPES);
    }

    public String storeAny(MultipartFile file, String folder) {
        return store(file, folder, null);
    }

    /**
     * Trả về URL cuối cùng để lưu DB từ một trong hai nguồn:
     * - Nếu {@code useFile = true}: bắt buộc có {@code file}, lưu file ảnh và trả về public URL.
     * - Nếu {@code useFile = false}: trả về {@code url} (string) do client cung cấp (có thể null).
     * <p>
     * Dùng cho các request có thuộc tính URL (avatar, logo, image, thumbnail...) muốn hỗ trợ
     * cả 2 chế độ: upload file hoặc gửi URL có sẵn, được chuyển đổi qua flag.
     *
     * @param file    MultipartFile ảnh (có thể null khi useFile=false)
     * @param url     URL string client cung cấp (có thể null khi useFile=true)
     * @param useFile true để dùng file upload, false để dùng URL string
     * @param folder  thư mục đích trong storage (vd: "avatars", "brands", "products"...)
     * @return URL cuối cùng để persist xuống DB, hoặc null nếu không có dữ liệu
     */
    public String resolveImageOrUrl(MultipartFile file, String url, boolean useFile, String folder) {
        if (useFile) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST,
                        "Cờ useFileUpload=true nhưng không có file đính kèm"
                );
            }
            return storeImage(file, folder);
        }
        return (StringUtils.hasText(url)) ? url.trim() : null;
    }

    public Resource loadByPublicUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Thiếu url");
        }

        String normalizedUrl = url.trim();
        if (!normalizedUrl.startsWith(publicUrlPrefix + "/")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "URL không hợp lệ");
        }

        String relative = normalizedUrl.substring((publicUrlPrefix + "/").length());
        Path resolved = baseDir.resolve(relative).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Đường dẫn không hợp lệ");
        }
        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "File không tồn tại");
        }
        return new FileSystemResource(resolved);
    }

    private String store(MultipartFile file, String folder, Set<String> allowedTypesOrNull) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Không có file");
        }
        if (allowedTypesOrNull != null && !allowedTypesOrNull.contains(file.getContentType())) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "Định dạng không hỗ trợ: " + file.getContentType()
            );
        }

        String safeFolder = StringUtils.hasText(folder) ? folder.trim().toLowerCase() : "misc";
        safeFolder = safeFolder.replaceAll("[^a-z0-9_-]", "-");

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String ext = extOf(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;

        Path dir = baseDir.resolve(safeFolder).resolve(yearMonth).normalize();
        if (!dir.startsWith(baseDir)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Thư mục không hợp lệ");
        }

        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return publicUrlPrefix + "/" + safeFolder + "/" + yearMonth + "/" + filename;
        } catch (IOException e) {
            log.error("Store file failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lưu file");
        }
    }

    private String extOf(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        String cleaned = filename.trim();
        int idx = cleaned.lastIndexOf('.');
        if (idx < 0) return "";
        String ext = cleaned.substring(idx).toLowerCase();
        if (!ext.matches("^\\.[a-z0-9]{1,10}$")) return "";
        return ext;
    }
}

