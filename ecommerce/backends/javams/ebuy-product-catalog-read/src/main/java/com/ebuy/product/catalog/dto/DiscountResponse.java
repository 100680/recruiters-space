package com.ebuy.product.catalog.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountResponse {
    private Long discountMethodId;
    private String methodName;
    private BigDecimal discountValue;
    private boolean isPercentage;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
}
