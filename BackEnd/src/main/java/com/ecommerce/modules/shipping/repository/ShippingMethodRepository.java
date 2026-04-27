package com.ecommerce.modules.shipping.repository;

import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.shipping.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {
    List<ShippingMethod> findByStatusOrderByBaseFeeAsc(CommonStatus status);
}
