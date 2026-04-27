package com.ecommerce.modules.user.repository;

import com.ecommerce.modules.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByIsDefaultDescIdAsc(Long userId);
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    Optional<UserAddress> findFirstByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.userId = :userId")
    void clearDefault(@Param("userId") Long userId);
}
