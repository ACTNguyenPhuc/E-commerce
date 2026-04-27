package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.ProductVariantValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantValueRepository extends JpaRepository<ProductVariantValue, ProductVariantValue.PvvId> {
    List<ProductVariantValue> findByVariantId(Long variantId);
    void deleteByVariantId(Long variantId);
}
