package com.ecommerce.modules.user.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.infrastructure.storage.FileStorageService;
import com.ecommerce.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.modules.user.dto.UserResponse;
import com.ecommerce.modules.user.entity.User;
import com.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String FOLDER = "avatars";

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req, MultipartFile file) {
        log.info("check" + String.valueOf(req.useFileUpload()));
        log.info(req.toString());

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (req.fullName() != null)     u.setFullName(req.fullName());
        if (req.phone() != null)        u.setPhone(req.phone());
        if (req.gender() != null)       u.setGender(req.gender());
        if (req.dateOfBirth() != null)  u.setDateOfBirth(req.dateOfBirth());

        boolean useFile = Boolean.TRUE.equals(req.useFileUpload());
        // Chỉ cập nhật avatar khi: dùng file, hoặc client truyền URL string khác null.
        // Nếu client không gửi file, không gửi avatarUrl, không bật cờ -> giữ nguyên avatar cũ.
        if (useFile || req.avatarUrl() != null) {

            String resolved = fileStorageService.resolveImageOrUrl(file, req.avatarUrl(), useFile, FOLDER);
            log.info("image" + resolved);
            u.setAvatarUrl(resolved);
            u.setUseFileUpload(useFile);
        }
        log.info("result");
        log.info(String.valueOf(u.getFullName() +  u.getAvatarUrl()));
        return UserResponse.from(u);
    }
}
