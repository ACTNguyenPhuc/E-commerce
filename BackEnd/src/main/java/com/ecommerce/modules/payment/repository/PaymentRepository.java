package com.ecommerce.modules.payment.repository;

import com.ecommerce.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    Optional<Payment> findByTransactionCode(String transactionCode);
}
