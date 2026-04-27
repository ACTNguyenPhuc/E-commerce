package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findByAttributeIdOrderByDisplayOrderAscIdAsc(Long attributeId);
    List<ProductAttributeValue> findByIdIn(List<Long> ids);
}
