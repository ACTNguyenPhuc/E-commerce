package com.ecommerce.modules.cart.repository;

import com.ecommerce.modules.cart.entity.Cart;
import com.ecommerce.modules.cart.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findFirstByUserIdAndStatusOrderByIdDesc(Long userId, CartStatus status);
    Optional<Cart> findFirstBySessionIdAndStatusOrderByIdDesc(String sessionId, CartStatus status);
}
