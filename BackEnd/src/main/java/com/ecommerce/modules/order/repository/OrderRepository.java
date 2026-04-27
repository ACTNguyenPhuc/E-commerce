package com.ecommerce.modules.order.repository;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);

    @Query("""
            SELECT o FROM Order o
            WHERE (:userId IS NULL OR o.userId = :userId)
              AND (:status IS NULL OR o.status = :status)
            """)
    Page<Order> search(@Param("userId") Long userId,
                       @Param("status") OrderStatus status,
                       Pageable pageable);

    List<Order> findByStatusAndPlacedAtBefore(OrderStatus status, LocalDateTime before);
}
