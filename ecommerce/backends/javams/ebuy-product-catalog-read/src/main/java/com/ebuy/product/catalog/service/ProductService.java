package com.ebuy.product.catalog.service;

import com.ebuy.product.catalog.dto.ProductResponse;
import com.ebuy.product.catalog.entity.Category;
import com.ebuy.product.catalog.entity.Product;
import com.ebuy.product.catalog.entity.ProductDiscount;
import com.ebuy.product.catalog.repository.CategoryRepository;
import com.ebuy.product.catalog.repository.ProductDiscountRepository;
import com.ebuy.product.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDiscountRepository discountRepository;

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name);
    }

    public List<Product> searchProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsDeletedFalse(categoryId);
    }

    public Product getProductById(Long productId) {
        Product product;
        product = productRepository.findByProductId(productId).orElse(null);
        return product;
    }

    public List<ProductDiscount> getActiveDiscounts(Long productId) {
        return discountRepository.findByProductIdAndActiveTrueAndIsDeletedFalse(productId);
    }

    public void evictAllCaches() {}
}
