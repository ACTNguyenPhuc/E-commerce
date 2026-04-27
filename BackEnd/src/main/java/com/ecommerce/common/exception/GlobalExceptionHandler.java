package com.ecommerce.common.exception;

import com.ecommerce.common.response.ApiError;
import com.ecommerce.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        ApiError error = ApiError.builder()
                .code(ex.getErrorCode().name())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getHttpStatus().value(), ex.getMessage(), List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toApiError)
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(v -> ApiError.builder()
                        .field(v.getPropertyPath().toString())
                        .code(ErrorCode.VALIDATION_ERROR.name())
                        .message(v.getMessage())
                        .build())
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Email hoặc mật khẩu không đúng",
                        List.of(ApiError.builder().code(ErrorCode.INVALID_CREDENTIALS.name()).message(ex.getMessage()).build())));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Chưa xác thực",
                        List.of(ApiError.builder().code(ErrorCode.UNAUTHORIZED.name()).message(ex.getMessage()).build())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "Bạn không có quyền truy cập",
                        List.of(ApiError.builder().code(ErrorCode.FORBIDDEN.name()).message(ex.getMessage()).build())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc",
                        List.of(ApiError.builder().code(ErrorCode.DUPLICATE_RESOURCE.name())
                                .message(ex.getMostSpecificCause().getMessage()).build())));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "Endpoint không tồn tại",
                        List.of(ApiError.builder().code(ErrorCode.NOT_FOUND.name()).message(ex.getMessage()).build())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Lỗi hệ thống",
                        List.of(ApiError.builder().code(ErrorCode.INTERNAL_ERROR.name()).message(ex.getMessage()).build())));
    }

    private ApiError toApiError(FieldError fe) {
        return ApiError.builder()
                .field(fe.getField())
                .code(ErrorCode.VALIDATION_ERROR.name())
                .message(fe.getDefaultMessage())
                .build();
    }
}
