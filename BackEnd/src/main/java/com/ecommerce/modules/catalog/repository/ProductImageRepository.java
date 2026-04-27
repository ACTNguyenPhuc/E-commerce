package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAscIdAsc(Long productId);
    void deleteByProductId(Long productId);

    @Modifying
    @Query("UPDATE ProductImage i SET i.isPrimary = false WHERE i.productId = :productId")
    void unsetPrimaryForProduct(@Param("productId") Long productId);
}
