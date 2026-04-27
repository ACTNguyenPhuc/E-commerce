package com.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND,
                "%s không tồn tại với id=%s".formatted(resource, id));
    }
}
