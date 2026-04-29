package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    List<Product> findTop10ByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(ProductStatus status);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:brandId    IS NULL OR p.brandId = :brandId)
              AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) >= :minPrice)
              AND (:maxPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) <= :maxPrice)
            """)
    Page<Product> search(@Param("status") ProductStatus status,
                         @Param("categoryId") Long categoryId,
                         @Param("brandId") Long brandId,
                         @Param("keyword") String keyword,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:brandId    IS NULL OR p.brandId = :brandId)
              AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) >= :minPrice)
              AND (:maxPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) <= :maxPrice)
            """)
    Page<Product> adminSearch(@Param("status") ProductStatus status,
                              @Param("categoryId") Long categoryId,
                              @Param("brandId") Long brandId,
                              @Param("keyword") String keyword,
                              @Param("minPrice") BigDecimal minPrice,
                              @Param("maxPrice") BigDecimal maxPrice,
                              Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:categoryIds IS NULL OR p.categoryId IN :categoryIds)
              AND (:brandId    IS NULL OR p.brandId = :brandId)
              AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (:minPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) >= :minPrice)
              AND (:maxPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) <= :maxPrice)
            """)
    Page<Product> searchByCategoryIds(@Param("status") ProductStatus status,
                                        @Param("categoryIds") List<Long> categoryIds,
                                        @Param("brandId") Long brandId,
                                        @Param("keyword") String keyword,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:categoryIds IS NULL OR p.categoryId IN :categoryIds)
              AND (:brandId    IS NULL OR p.brandId = :brandId)
              AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) >= :minPrice)
              AND (:maxPrice   IS NULL OR COALESCE(p.salePrice, p.basePrice) <= :maxPrice)
            """)
    Page<Product> adminSearchByCategoryIds(@Param("status") ProductStatus status,
                                             @Param("categoryIds") List<Long> categoryIds,
                                             @Param("brandId") Long brandId,
                                             @Param("keyword") String keyword,
                                             @Param("minPrice") BigDecimal minPrice,
                                             @Param("maxPrice") BigDecimal maxPrice,
                                             Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Product p SET p.soldCount = p.soldCount + :qty WHERE p.id = :id")
    void incrementSoldCount(@Param("id") Long id, @Param("qty") long qty);
}
