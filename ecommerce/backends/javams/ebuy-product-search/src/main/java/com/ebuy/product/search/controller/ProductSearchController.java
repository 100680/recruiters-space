package com.ebuy.product.search.controller;

import com.ebuy.product.search.dto.AutocompleteRequest;
import com.ebuy.product.search.dto.ProductSearchRequest;
import com.ebuy.product.search.dto.ProductSearchResponse;
import com.ebuy.product.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @PostMapping("/search")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @Valid @RequestBody ProductSearchRequest request) throws IOException {
        ProductSearchResponse response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponse> searchProductsGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "true") Boolean inStockOnly,
            @RequestParam(defaultValue = "relevance") String sortBy) throws IOException {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setQuery(query);
        request.setPage(page);
        request.setSize(size);
        request.setCategories(categories);
        request.setBrands(brands);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setInStockOnly(inStockOnly);
        request.setSortBy(sortBy);

        ProductSearchResponse response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(
            @Valid @RequestBody AutocompleteRequest request) throws IOException {
        List<String> suggestions = productSearchService.getAutocompleteSuggestions(request);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestionsGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int size) throws IOException {

        AutocompleteRequest request = new AutocompleteRequest();
        request.setQuery(query);
        request.setSize(size);

        List<String> suggestions = productSearchService.getAutocompleteSuggestions(request);
        return ResponseEntity.ok(suggestions);
    }
}