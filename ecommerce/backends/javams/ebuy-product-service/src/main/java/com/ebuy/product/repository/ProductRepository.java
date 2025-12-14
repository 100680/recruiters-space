package com.ebuy.product.repository;

import com.ebuy.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductIdAndIsDeletedFalse(Long productId);

    Optional<Product> findBySkuAndIsDeletedFalse(String sku);

    Page<Product> findByIsDeletedFalse(Pageable pageable);

    Page<Product> findByCategoryCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND " +
            "p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.stock <= p.reorderLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.stock = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false")
    long countActiveProducts();

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isDeleted = false")
    BigDecimal calculateAveragePrice();

    boolean existsBySkuAndIsDeletedFalse(String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.isDeleted = false")
    Page<Product> findAllWithCategory(Pageable pageable);
}