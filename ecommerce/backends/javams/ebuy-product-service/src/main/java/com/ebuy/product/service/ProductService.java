package com.ebuy.product.service;

import com.ebuy.product.dto.request.ProductCreateRequest;
import com.ebuy.product.dto.request.ProductUpdateRequest;
import com.ebuy.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request, String createdBy);

    ProductResponse updateProduct(Long productId, ProductUpdateRequest request, String modifiedBy);

    ProductResponse getProduct(Long productId);

    ProductResponse getProductBySku(String sku);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponse> searchProducts(String searchTerm, Pageable pageable);

    Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    void deleteProduct(Long productId, String deletedBy);

    void updateStock(Long productId, Integer newStock, String modifiedBy);

    List<ProductResponse> getLowStockProducts();

    List<ProductResponse> getOutOfStockProducts();

    BigDecimal getEffectivePrice(Long productId);

    long getTotalProductCount();

    BigDecimal getAverageProductPrice();
}
