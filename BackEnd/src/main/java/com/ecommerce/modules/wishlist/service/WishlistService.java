package com.ecommerce.modules.wishlist.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.catalog.dto.ProductResponse;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import com.ecommerce.modules.wishlist.entity.Wishlist;
import com.ecommerce.modules.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository repo;
    private final ProductRepository productRepo;

    @Transactional(readOnly = true)
    public List<ProductResponse> list(Long userId) {
        List<Long> productIds = repo.findByUserId(userId).stream().map(Wishlist::getProductId).toList();
        return productRepo.findAllById(productIds).stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public void add(Long userId, Long productId) {
        if (productRepo.findById(productId).isEmpty()) {
            throw new ResourceNotFoundException("Product", productId);
        }
        if (!repo.existsByUserIdAndProductId(userId, productId)) {
            repo.save(Wishlist.builder().userId(userId).productId(productId).build());
        }
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        repo.deleteByUserIdAndProductId(userId, productId);
    }
}
