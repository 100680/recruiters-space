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
@Document(collection = "discount_methods")
public class DiscountMethod {
    @Id
    private Long discountMethodId;
    private String methodName;
    private String description;
    private boolean isPercentage;
    private BigDecimal maxDiscountValue;
    private Instant createdAt;
    private Instant modifiedAt;
    private Instant deletedAt;
    private boolean isDeleted;
    private String createdBy;
    private String modifiedBy;
    private Long rowVersion;
}