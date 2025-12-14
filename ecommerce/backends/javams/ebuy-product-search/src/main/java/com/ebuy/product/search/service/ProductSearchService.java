package com.ebuy.product.search.service;

import com.ebuy.product.search.dto.AutocompleteRequest;
import com.ebuy.product.search.dto.ProductSearchRequest;
import com.ebuy.product.search.dto.ProductSearchResponse;
import com.ebuy.product.search.entity.Product;
import com.ebuy.product.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    public ProductSearchResponse searchProducts(ProductSearchRequest request) throws IOException {
        log.info("Searching products with query: {}", request.getQuery());

        // Add business logic here if needed (caching, analytics, etc.)
        int paginationOffset = calculatePaginationOffset(request.getPage(), request.getSize());
        long startTime = System.currentTimeMillis();

        List<Product> products = productSearchRepository.searchProducts(request.getQuery(), paginationOffset, request.getSize());

        ProductSearchResponse response = buildSearchResponse(products, request, paginationOffset, startTime);

        log.info("Found {} products for query: {}", response.getTotalHits(), request.getQuery());
        return response;
    }

    private int calculatePaginationOffset(int page, int size) {
        return page * size;
    }

    private ProductSearchResponse buildSearchResponse(List<Product> products, ProductSearchRequest request,
                                                      int paginationOffset, long startTime) {
        long timeTaken = System.currentTimeMillis() - startTime;

        return new ProductSearchResponse(
                products,
                products.size(), // This should ideally come from repository with actual total count
                paginationOffset,
                request.getSize(),
                timeTaken,
                request.getQuery()
        );
    }

    public List<String> getAutocompleteSuggestions(AutocompleteRequest request) throws IOException {
        log.info("Getting autocomplete suggestions for: {}", request.getQuery());

        List<String> suggestions = productSearchRepository.getAutocompleteSuggestions(request.getCategoryId(), request.getQuery(), request.getSize());

        log.info("Found {} autocomplete suggestions", suggestions.size());
        return suggestions;
    }
}