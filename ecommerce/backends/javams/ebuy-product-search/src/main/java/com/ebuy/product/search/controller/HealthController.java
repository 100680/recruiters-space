package com.ebuy.product.search.controller;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final OpenSearchClient openSearchClient;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "product-search-service");

        try {
            // Check OpenSearch connectivity
            openSearchClient.ping();
            health.put("opensearch", "UP");
        } catch (Exception e) {
            health.put("opensearch", "DOWN");
            health.put("opensearch_error", e.getMessage());
        }

        return ResponseEntity.ok(health);
    }
}