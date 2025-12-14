package com.ebuy.product.search.dto;

import com.ebuy.product.search.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    private List<Product> products;
    private long totalHits;
    private int from;
    private int size;
    private long took; // time taken in milliseconds
    private String query;
}