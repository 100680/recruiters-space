package com.ebuy.product.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "product_discounts", schema = "product",
        indexes = {
                @Index(name = "ix_product_discounts_product_id", columnList = "product_id"),
                @Index(name = "ix_product_discounts_active", columnList = "active"),
                @Index(name = "ix_product_discounts_is_deleted", columnList = "is_deleted"),
                @Index(name = "ix_product_discounts_dates", columnList = "start_date, end_date"),
                @Index(name = "ix_active_discounts_by_product", columnList = "product_id, discount_value")
        })
@EntityListeners(AuditingEntityListener.class)
public class ProductDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_discount_id")
    private Long productDiscountId;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Discount method is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_method_id", nullable = false)
    private DiscountMethod discountMethod;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount value must be non-negative")
    @Column(name = "discount_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal discountValue;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    private OffsetDateTime modifiedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Size(max = 100)
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Size(max = 100)
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @NotNull
    @Column(name = "row_version", nullable = false)
    private Long rowVersion = 1L;

    // Constructors
    public ProductDiscount() {
    }

    public ProductDiscount(Product product, DiscountMethod discountMethod,
                           BigDecimal discountValue, OffsetDateTime startDate, OffsetDateTime endDate) {
        this.product = product;
        this.discountMethod = discountMethod;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getProductDiscountId() {
        return productDiscountId;
    }
    public void setProductDiscountId(Long productDiscountId) {
        this.productDiscountId = productDiscountId;
    }

    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public DiscountMethod getDiscountMethod() {
        return discountMethod;
    }
    public void setDiscountMethod(DiscountMethod discountMethod) {
        this.discountMethod = discountMethod;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getModifiedAt() {
        return modifiedAt;
    }
    public void setModifiedAt(OffsetDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getRowVersion() {
        return rowVersion;
    }
    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }
}