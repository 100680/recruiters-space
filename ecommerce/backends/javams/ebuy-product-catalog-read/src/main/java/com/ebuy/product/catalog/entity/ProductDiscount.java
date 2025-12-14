package com.ebuy.product.catalog.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "product_discounts")
public class ProductDiscount {
    @Id
    private Long productDiscountId;
    private Long productId;
    private Long discountMethodId;
    private BigDecimal discountValue;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private Instant createdAt;
    private Instant modifiedAt;
    private Instant deletedAt;
    private boolean isDeleted;
    private String createdBy;
    private String modifiedBy;
    private Long rowVersion;
}