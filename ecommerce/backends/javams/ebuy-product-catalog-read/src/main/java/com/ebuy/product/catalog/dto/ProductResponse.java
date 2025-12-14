package com.ebuy.product.catalog.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice; // price after active discount
    private Integer stock;
    private Integer reorderLevel;
    private Long categoryId;
    private String categoryName;        // added
    private String imageUrl;
    private String sku;
    private Instant createdAt;
}