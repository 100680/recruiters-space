package com.ebuy.product.repository;


import com.ebuy.product.entity.ProductDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {

    Page<ProductDiscount> findByIsDeletedFalse(Pageable pageable);

    List<ProductDiscount> findByProductProductIdAndIsDeletedFalseOrderByDiscountValueDesc(Long productId);

    @Query("SELECT pd FROM ProductDiscount pd WHERE pd.product.productId = :productId " +
            "AND pd.active = true AND pd.isDeleted = false " +
            "AND pd.startDate <= :currentTime " +
            "AND (pd.endDate IS NULL OR pd.endDate > :currentTime) " +
            "ORDER BY pd.discountValue DESC")
    Optional<ProductDiscount> findActiveDiscountByProductId(@Param("productId") Long productId,
                                                            @Param("currentTime") OffsetDateTime currentTime);

    @Query("SELECT pd FROM ProductDiscount pd WHERE pd.active = true AND pd.isDeleted = false " +
            "AND pd.startDate <= :currentTime " +
            "AND (pd.endDate IS NULL OR pd.endDate > :currentTime)")
    List<ProductDiscount> findAllActiveDiscounts(@Param("currentTime") OffsetDateTime currentTime);

    @Query("SELECT pd FROM ProductDiscount pd WHERE pd.active = true AND pd.isDeleted = false " +
            "AND pd.endDate < :currentTime")
    List<ProductDiscount> findExpiredActiveDiscounts(@Param("currentTime") OffsetDateTime currentTime);

    @Query("SELECT COUNT(pd) FROM ProductDiscount pd WHERE pd.discountMethod.discountMethodId = :discountMethodId " +
            "AND pd.isDeleted = false")
    long countByDiscountMethodId(@Param("discountMethodId") Long discountMethodId);
}
