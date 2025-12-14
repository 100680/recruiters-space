package com.ebuy.product.repository;

import com.ebuy.product.entity.DiscountMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountMethodRepository extends JpaRepository<DiscountMethod, Long> {

    Optional<DiscountMethod> findByMethodNameIgnoreCaseAndIsDeletedFalse(String methodName);

    List<DiscountMethod> findByIsDeletedFalseOrderByMethodName();

    Page<DiscountMethod> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT dm FROM DiscountMethod dm WHERE dm.isDeleted = false AND " +
            "LOWER(dm.methodName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DiscountMethod> searchByMethodName(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByMethodNameIgnoreCaseAndIsDeletedFalse(String methodName);
}
