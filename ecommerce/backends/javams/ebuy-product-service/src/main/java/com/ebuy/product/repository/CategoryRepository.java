package com.ebuy.product.repository;


import com.ebuy.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    List<Category> findByIsDeletedFalseOrderByName();

    Page<Category> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isDeleted = false AND " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId AND p.isDeleted = false")
    long countActiveProductsByCategory(@Param("categoryId") Long categoryId);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);
}
