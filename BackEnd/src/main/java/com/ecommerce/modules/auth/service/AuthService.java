package com.ecommerce.modules.auth.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.auth.dto.*;
import com.ecommerce.modules.user.dto.UserResponse;
import com.ecommerce.modules.user.entity.Role;
import com.ecommerce.modules.user.entity.User;
import com.ecommerce.modules.user.entity.UserStatus;
import com.ecommerce.modules.user.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwt;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        if (req.phone() != null && userRepository.existsByPhone(req.phone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng");
        }
        User user = User.builder()
                .email(req.email())
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(Role.customer)
                .status(UserStatus.active)
                .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        user.setLastLoginAt(LocalDateTime.now());
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        if (!jwt.isValid(req.refreshToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
        }
        Claims claims = jwt.parse(req.refreshToken());
        if (!JwtTokenProvider.TYPE_REFRESH.equals(claims.get(JwtTokenProvider.TOKEN_TYPE, String.class))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Token không phải refresh token");
        }
        Long userId = Long.valueOf(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Người dùng không tồn tại"));
        return buildAuthResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwt.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = jwt.generateRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwt.getAccessTtlSeconds())
                .user(UserResponse.from(user))
                .build();
    }
}
