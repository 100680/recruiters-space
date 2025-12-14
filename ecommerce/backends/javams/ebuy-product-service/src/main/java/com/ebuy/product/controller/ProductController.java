package com.ebuy.product.controller;


import com.ebuy.product.dto.request.ProductCreateRequest;
import com.ebuy.product.dto.request.ProductUpdateRequest;
import com.ebuy.product.dto.response.ProductResponse;
import com.ebuy.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@Validated
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Creating product: {} by user: {}", request.getName(), userId);
        ProductResponse response = productService.createProduct(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update an existing product", description = "Updates an existing product with the provided information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product or category not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent modification detected")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable @Min(1) Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Updating product ID: {} by user: {}", productId, userId);
        ProductResponse response = productService.updateProduct(productId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Product ID") @PathVariable @Min(1) Long productId) {

        logger.debug("Fetching product ID: {}", productId);
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable @NotBlank String sku) {

        logger.debug("Fetching product SKU: {}", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.debug("Fetching all products - page: {}, size: {}, sort: {}", page, size, sort);
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieves all products in a specific category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable @Min(1) Long categoryId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.debug("Fetching products by category ID: {} - page: {}, size: {}", categoryId, page, size);
        Page<ProductResponse> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by name or description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Search term") @RequestParam @NotBlank String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.debug("Searching products with term: {} - page: {}, size: {}", q, page, size);
        Page<ProductResponse> response = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Retrieves products within a specific price range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<Page<ProductResponse>> getProductsByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "price") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.debug("Fetching products by price range: {} - {} - page: {}, size: {}", minPrice, maxPrice, page, size);
        Page<ProductResponse> response = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product", description = "Soft deletes a product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable @Min(1) Long productId,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Deleting product ID: {} by user: {}", productId, userId);
        productService.deleteProduct(productId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/stock")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity for a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> updateStock(
            @Parameter(description = "Product ID") @PathVariable @Min(1) Long productId,
            @RequestBody Map<String, Integer> stockUpdate,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        Integer newStock = stockUpdate.get("stock");
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("Stock must be a non-negative number");
        }

        logger.info("Updating stock for product ID: {} to: {} by user: {}", productId, newStock, userId);
        productService.updateStock(productId, newStock, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieves products with stock at or below reorder level")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        logger.debug("Fetching low stock products");
        List<ProductResponse> response = productService.getLowStockProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock products", description = "Retrieves products with zero stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Out of stock products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponse>> getOutOfStockProducts() {
        logger.debug("Fetching out of stock products");
        List<ProductResponse> response = productService.getOutOfStockProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/effective-price")
    @Operation(summary = "Get effective price", description = "Gets the effective price of a product including discounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Effective price retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Map<String, BigDecimal>> getEffectivePrice(
            @Parameter(description = "Product ID") @PathVariable @Min(1) Long productId) {

        logger.debug("Fetching effective price for product ID: {}", productId);
        BigDecimal effectivePrice = productService.getEffectivePrice(productId);
        return ResponseEntity.ok(Map.of("effectivePrice", effectivePrice));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get product statistics", description = "Retrieves product statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getProductStats() {
        logger.debug("Fetching product statistics");

        long totalCount = productService.getTotalProductCount();
        BigDecimal averagePrice = productService.getAverageProductPrice();

        Map<String, Object> stats = Map.of(
                "totalProductCount", totalCount,
                "averagePrice", averagePrice
        );

        return ResponseEntity.ok(stats);
    }
}
