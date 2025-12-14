package com.ebuy.product.catalog.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "product_price_history")
public class ProductPriceHistory {
    @Id
    private Long priceHistoryId;
    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String priceChangeReason;
    private Instant changedAt;
    private String changedBy;
    private UUID correlationId;
}