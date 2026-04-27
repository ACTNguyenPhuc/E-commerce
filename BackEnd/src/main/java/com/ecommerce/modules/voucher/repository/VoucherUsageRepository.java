package com.ecommerce.modules.voucher.repository;

import com.ecommerce.modules.voucher.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    long countByVoucherIdAndUserId(Long voucherId, Long userId);
}
