package com.ecommerce.modules.order.repository;

import com.ecommerce.modules.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    Optional<OrderItem> findByIdAndOrderId(Long id, Long orderId);
}
