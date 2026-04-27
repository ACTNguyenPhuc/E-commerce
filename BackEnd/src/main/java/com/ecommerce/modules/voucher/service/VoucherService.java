package com.ecommerce.modules.voucher.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.voucher.dto.*;
import com.ecommerce.modules.voucher.entity.DiscountType;
import com.ecommerce.modules.voucher.entity.Voucher;
import com.ecommerce.modules.voucher.entity.VoucherStatus;
import com.ecommerce.modules.voucher.repository.VoucherRepository;
import com.ecommerce.modules.voucher.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository repo;
    private final VoucherUsageRepository usageRepo;

    @Transactional(readOnly = true)
    public List<VoucherResponse> activeVouchers() {
        return repo.findActiveAt(VoucherStatus.active, LocalDateTime.now())
                .stream().map(VoucherResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VoucherValidationResult validate(Long userId, ValidateVoucherRequest req) {
        Voucher v = repo.findByCode(req.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VOUCHER, HttpStatus.NOT_FOUND, "Mã voucher không tồn tại"));
        return validateAndCalc(v, req.subtotal(), userId);
    }

    public VoucherValidationResult validateAndCalc(Voucher v, BigDecimal subtotal, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        if (v.getStatus() != VoucherStatus.active) {
            throw new BusinessException(ErrorCode.INVALID_VOUCHER, HttpStatus.UNPROCESSABLE_ENTITY, "Voucher không hoạt động");
        }
        if (now.isBefore(v.getStartDate()) || now.isAfter(v.getEndDate())) {
            throw new BusinessException(ErrorCode.VOUCHER_EXPIRED, HttpStatus.UNPROCESSABLE_ENTITY, "Voucher hết hạn hoặc chưa bắt đầu");
        }
        if (v.getMinOrderAmount() != null && subtotal.compareTo(v.getMinOrderAmount()) < 0) {
            throw new BusinessException(ErrorCode.INVALID_VOUCHER, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn tối thiểu " + v.getMinOrderAmount() + " để dùng voucher này");
        }
        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            throw new BusinessException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED, HttpStatus.UNPROCESSABLE_ENTITY, "Voucher đã hết lượt dùng");
        }
        if (userId != null && v.getUsageLimitPerUser() != null) {
            long used = usageRepo.countByVoucherIdAndUserId(v.getId(), userId);
            if (used >= v.getUsageLimitPerUser()) {
                throw new BusinessException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED, HttpStatus.UNPROCESSABLE_ENTITY,
                        "Bạn đã dùng voucher này tối đa số lần cho phép");
            }
        }
        BigDecimal discount;
        if (v.getDiscountType() == DiscountType.percentage) {
            discount = subtotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (v.getMaxDiscountAmount() != null && discount.compareTo(v.getMaxDiscountAmount()) > 0) {
                discount = v.getMaxDiscountAmount();
            }
        } else {
            discount = v.getDiscountValue().min(subtotal);
        }
        return VoucherValidationResult.builder()
                .valid(true)
                .code(v.getCode())
                .discountAmount(discount)
                .message("Áp dụng thành công")
                .build();
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> listAll() {
        return repo.findAll().stream().map(VoucherResponse::from).toList();
    }

    @Transactional
    public VoucherResponse create(VoucherRequest req) {
        if (repo.existsByCode(req.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Mã voucher đã tồn tại");
        }
        if (req.endDate().isBefore(req.startDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "endDate phải lớn hơn startDate");
        }
        Voucher v = Voucher.builder()
                .code(req.code()).name(req.name()).description(req.description())
                .discountType(req.discountType()).discountValue(req.discountValue())
                .minOrderAmount(req.minOrderAmount() != null ? req.minOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(req.maxDiscountAmount())
                .usageLimit(req.usageLimit()).usageLimitPerUser(req.usageLimitPerUser())
                .startDate(req.startDate()).endDate(req.endDate())
                .status(req.status() != null ? req.status() : VoucherStatus.active)
                .build();
        return VoucherResponse.from(repo.save(v));
    }

    @Transactional
    public VoucherResponse update(Long id, VoucherRequest req) {
        Voucher v = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher", id));
        v.setName(req.name());
        v.setDescription(req.description());
        v.setDiscountType(req.discountType());
        v.setDiscountValue(req.discountValue());
        if (req.minOrderAmount() != null) v.setMinOrderAmount(req.minOrderAmount());
        v.setMaxDiscountAmount(req.maxDiscountAmount());
        v.setUsageLimit(req.usageLimit());
        v.setUsageLimitPerUser(req.usageLimitPerUser());
        v.setStartDate(req.startDate());
        v.setEndDate(req.endDate());
        if (req.status() != null) v.setStatus(req.status());
        return VoucherResponse.from(v);
    }

    @Transactional
    public void delete(Long id) {
        Voucher v = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher", id));
        repo.delete(v);
    }
}
