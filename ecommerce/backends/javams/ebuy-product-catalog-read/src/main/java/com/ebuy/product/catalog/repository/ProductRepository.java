package com.ebuy.product.catalog.repository;

import com.ebuy.product.catalog.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    // This method will now work correctly
    Optional<Product> findByProductId(Long productId);

    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);
}