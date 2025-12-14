package com.ebuy.product.search.controller;

import com.ebuy.product.search.entity.Product;
import com.ebuy.product.search.repository.ProductSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.HealthRequest;
import org.opensearch.client.opensearch.cluster.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class OpenSearchTestController {

    @Autowired
    private OpenSearchClient openSearchClient;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Testing OpenSearch connection...");

            HealthRequest healthRequest = new HealthRequest.Builder().build();
            HealthResponse healthResponse = openSearchClient.cluster().health(healthRequest);

            response.put("status", "success");
            response.put("cluster_name", healthResponse.clusterName());
            response.put("cluster_status", healthResponse.status().toString());
            response.put("number_of_nodes", healthResponse.numberOfNodes());
            response.put("number_of_data_nodes", healthResponse.numberOfDataNodes());

            log.info("OpenSearch connection successful!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("OpenSearch connection failed", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/index-sample")
    public ResponseEntity<Map<String, Object>> indexSampleProduct() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Creating and indexing sample product...");

            // Create a simple sample product
            Product sampleProduct = new Product();
            sampleProduct.setProductId(1L);
            sampleProduct.setName("Sample iPhone 15");
            sampleProduct.setDescription("Latest iPhone with advanced features");
            sampleProduct.setPrice(999.99);
            sampleProduct.setDiscountedPrice(899.99);
            sampleProduct.setDiscountPercentage(10.0f);
            sampleProduct.setHasActiveDiscount(true);
            sampleProduct.setStock(50);
            sampleProduct.setReorderLevel(10);
            sampleProduct.setIsInStock(true);
            sampleProduct.setStockStatus("IN_STOCK");
            sampleProduct.setSku("IPH15-001");
            sampleProduct.setImageUrl("https://example.com/iphone15.jpg");
            sampleProduct.setTags(Arrays.asList("smartphone", "apple", "electronics"));
            sampleProduct.setBrand("Apple");
            sampleProduct.setPopularityScore(85.5f);
            sampleProduct.setSearchKeywords("iphone smartphone apple mobile phone");
            sampleProduct.setCreatedAt(LocalDateTime.now());
            sampleProduct.setModifiedAt(LocalDateTime.now());
            sampleProduct.setIsDeleted(false);
            sampleProduct.setIsActive(true);
            sampleProduct.setSortOrder(1);
            sampleProduct.setSearchBoost(1.2f);

            // Try to index the product
            productSearchRepository.indexProduct(sampleProduct);

            response.put("status", "success");
            response.put("message", "Sample product indexed successfully");
            response.put("product_id", sampleProduct.getProductId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to index sample product", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            // Add stack trace for debugging
            response.put("stack_trace", Arrays.toString(e.getStackTrace()));

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getProductCount() {
        Map<String, Object> response = new HashMap<>();

        try {
            long count = productSearchRepository.getTotalProductCount();
            response.put("status", "success");
            response.put("total_products", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get product count", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}