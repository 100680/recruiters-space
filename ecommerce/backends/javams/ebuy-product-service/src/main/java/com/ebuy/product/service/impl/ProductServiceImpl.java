package com.ebuy.product.service.impl;

import com.ebuy.product.dto.request.ProductCreateRequest;
import com.ebuy.product.dto.request.ProductUpdateRequest;
import com.ebuy.product.dto.response.ProductResponse;
import com.ebuy.product.entity.Category;
import com.ebuy.product.entity.Product;
import com.ebuy.product.exception.CategoryNotFoundException;
import com.ebuy.product.exception.ProductNotFoundException;
import com.ebuy.product.repository.CategoryRepository;
import com.ebuy.product.repository.ProductRepository;
import com.ebuy.product.service.ProductService;
import com.ebuy.product.util.ProductMapper;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductCreateRequest request, String createdBy) {
        logger.info("Creating product: {}", request.getName());

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Check if SKU already exists
        if (request.getSku() != null && productRepository.existsBySkuAndIsDeletedFalse(request.getSku())) {
            throw new DataIntegrityViolationException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setCreatedBy(createdBy);

        try {
            Product savedProduct = productRepository.save(product);
            logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
            return productMapper.toResponse(savedProduct);
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage());
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request, String modifiedBy) {
        logger.info("Updating product with ID: {}", productId);

        Product existingProduct = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Check optimistic locking
        if (!existingProduct.getRowVersion().equals(request.getRowVersion())) {
            throw new OptimisticLockException("Product has been modified by another user. Please refresh and try again.");
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> !c.getIsDeleted())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));
            existingProduct.setCategory(category);
        }

        // Update other fields
        productMapper.updateEntityFromRequest(request, existingProduct);
        existingProduct.setModifiedBy(modifiedBy);

        try {
            Product updatedProduct = productRepository.save(existingProduct);
            logger.info("Product updated successfully with ID: {}", updatedProduct.getProductId());
            return productMapper.toResponse(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage());
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    @Cacheable(value = "product", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        logger.debug("Fetching product with ID: {}", productId);

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        return productMapper.toResponse(product);
    }

    @Override
    @Cacheable(value = "product", key = "'sku:' + #sku")
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        logger.debug("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySkuAndIsDeletedFalse(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        return productMapper.toResponse(product);
    }

    @Override
    @Cacheable(value = "products")
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        logger.debug("Fetching all products with pagination: {}", pageable);

        Page<Product> products = productRepository.findByIsDeletedFalse(pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        logger.debug("Fetching products by category ID: {} with pagination: {}", categoryId, pageable);

        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }

        Page<Product> products = productRepository.findByCategoryCategoryIdAndIsDeletedFalse(categoryId, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String searchTerm, Pageable pageable) {
        logger.debug("Searching products with term: {} and pagination: {}", searchTerm, pageable);

        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        logger.debug("Fetching products in price range: {} - {} with pagination: {}", minPrice, maxPrice, pageable);

        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long productId, String deletedBy) {
        logger.info("Soft deleting product with ID: {}", productId);

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.markDeleted();
        product.setModifiedBy(deletedBy);

        try {
            productRepository.save(product);
            logger.info("Product soft deleted successfully with ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage());
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void updateStock(Long productId, Integer newStock, String modifiedBy) {
        logger.info("Updating stock for product ID: {} to: {}", productId, newStock);

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.setStock(newStock);
        product.setModifiedBy(modifiedBy);

        try {
            productRepository.save(product);
            logger.info("Stock updated successfully for product ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error updating stock: {}", e.getMessage());
            throw new RuntimeException("Failed to update stock", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        logger.debug("Fetching low stock products");

        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        logger.debug("Fetching out of stock products");

        List<Product> products = productRepository.findOutOfStockProducts();
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "effectivePrice", key = "#productId")
    @Transactional(readOnly = true)
    public BigDecimal getEffectivePrice(Long productId) {
        logger.debug("Calculating effective price for product ID: {}", productId);

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // For now, return base price. This can be extended to include discount calculations
        // by calling the PostgreSQL function get_effective_price
        return product.getPrice();
    }

    @Override
    @Cacheable(value = "productStats", key = "'totalCount'")
    @Transactional(readOnly = true)
    public long getTotalProductCount() {
        return productRepository.countActiveProducts();
    }

    @Override
    @Cacheable(value = "productStats", key = "'averagePrice'")
    @Transactional(readOnly = true)
    public BigDecimal getAverageProductPrice() {
        return Optional.ofNullable(productRepository.calculateAveragePrice())
                .orElse(BigDecimal.ZERO);
    }
}