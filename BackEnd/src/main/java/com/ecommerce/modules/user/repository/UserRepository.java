package com.ecommerce.modules.user.repository;

import com.ecommerce.modules.user.entity.Role;
import com.ecommerce.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%) AND (:role IS NULL OR u.role = :role)")
    Page<User> search(String keyword, Role role, Pageable pageable);
}
