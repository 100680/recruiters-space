package com.ebuy.product.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_price_history", schema = "product",
        indexes = {
                @Index(name = "ix_price_history_product_id", columnList = "product_id, changed_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_history_id")
    private Long priceHistoryId;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_price", precision = 15, scale = 4)
    private BigDecimal oldPrice;

    @NotNull(message = "New price is required")
    @Column(name = "new_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal newPrice;

    @Size(max = 100, message = "Price change reason must be less than 100 characters")
    @Column(name = "price_change_reason", length = 100)
    private String priceChangeReason;

    @CreatedDate
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    @Size(max = 100)
    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "correlation_id")
    private UUID correlationId;

    // Constructors
    public ProductPriceHistory() {
        this.correlationId = UUID.randomUUID();
    }

    public ProductPriceHistory(Product product, BigDecimal oldPrice, BigDecimal newPrice,
                               String priceChangeReason, String changedBy) {
        this();
        this.product = product;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.priceChangeReason = priceChangeReason;
        this.changedBy = changedBy;
    }

    // Getters and Setters
    public Long getPriceHistoryId() {
        return priceHistoryId;
    }

    public void setPriceHistoryId(Long priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }

    public String getPriceChangeReason() {
        return priceChangeReason;
    }

    public void setPriceChangeReason(String priceChangeReason) {
        this.priceChangeReason = priceChangeReason;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    // Utility methods
    public BigDecimal getPriceChange() {
        if (oldPrice == null) return newPrice;
        return newPrice.subtract(oldPrice);
    }

    public BigDecimal getPriceChangePercentage() {
        if (oldPrice == null || oldPrice.equals(BigDecimal.ZERO)) return BigDecimal.ZERO;
        return getPriceChange().divide(oldPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPriceHistory)) return false;
        ProductPriceHistory that = (ProductPriceHistory) o;
        return priceHistoryId != null && priceHistoryId.equals(that.priceHistoryId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductPriceHistory{" +
                "priceHistoryId=" + priceHistoryId +
                ", oldPrice=" + oldPrice +
                ", newPrice=" + newPrice +
                ", changedAt=" + changedAt +
                '}';
    }
}
