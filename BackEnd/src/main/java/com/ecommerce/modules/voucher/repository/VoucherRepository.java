package com.ecommerce.modules.voucher.repository;

import com.ecommerce.modules.voucher.entity.Voucher;
import com.ecommerce.modules.voucher.entity.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.status = :status AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findActiveAt(@Param("status") VoucherStatus status, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 WHERE v.id = :id")
    void incrementUsedCount(@Param("id") Long id);
}
