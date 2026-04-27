package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    boolean existsBySlug(String slug);
}
