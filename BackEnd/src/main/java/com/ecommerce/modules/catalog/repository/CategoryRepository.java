package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Category> findByStatusOrderByDisplayOrderAscIdAsc(CommonStatus status);
    List<Category> findByParentId(Long parentId);
}
