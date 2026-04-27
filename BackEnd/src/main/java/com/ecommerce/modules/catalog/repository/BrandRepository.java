package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.Brand;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Brand> findByStatusOrderByNameAsc(CommonStatus status);
}
