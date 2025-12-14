package com.ebuy.product.catalog.repository;

import com.ebuy.product.catalog.entity.ProductDiscount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProductDiscountRepository extends MongoRepository<ProductDiscount, String> {
    List<ProductDiscount> findByProductIdAndActiveTrueAndIsDeletedFalse(Long productId);
}
