
// ProductSearchRequest.java
package com.ebuy.product.search.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    @NotBlank(message = "Search query cannot be empty")
    private String query;

    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    private int size = 20;

    private List<String> categories;
    private List<String> brands;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStockOnly = true;
    private String sortBy = "relevance"; // relevance, price_asc, price_desc, rating, popularity
}