package com.ebuy.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "discount_methods", schema = "product",
        indexes = {
                @Index(name = "uq_discount_methods_name_active", columnList = "method_name", unique = true),
                @Index(name = "ix_discount_methods_is_deleted", columnList = "is_deleted")
        })
@EntityListeners(AuditingEntityListener.class)
public class DiscountMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_method_id")
    private Long discountMethodId;

    @NotBlank(message = "Discount method name is required")
    @Size(max = 50, message = "Method name must be less than 50 characters")
    @Column(name = "method_name", nullable = false, length = 50)
    private String methodName;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "is_percentage", nullable = false)
    private Boolean isPercentage = false;

    @Column(name = "max_discount_value", precision = 15, scale = 4)
    private BigDecimal maxDiscountValue;

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

    @OneToMany(mappedBy = "discountMethod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductDiscount> productDiscounts = new ArrayList<>();

    public DiscountMethod() {}

    public DiscountMethod(String methodName, String description, Boolean isPercentage) {
        this.methodName = methodName;
        this.description = description;
        this.isPercentage = isPercentage;
    }

    public Long getDiscountMethodId() {
        return discountMethodId;
    }

    public void setDiscountMethodId(Long discountMethodId) {
        this.discountMethodId = discountMethodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPercentage() {
        return isPercentage;
    }

    public void setIsPercentage(Boolean isPercentage) {
        this.isPercentage = isPercentage;
    }

    public BigDecimal getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public void setMaxDiscountValue(BigDecimal maxDiscountValue) {
        this.maxDiscountValue = maxDiscountValue;
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

    public List<ProductDiscount> getProductDiscounts() {
        return productDiscounts;
    }

    public void setProductDiscounts(List<ProductDiscount> productDiscounts) {
        this.productDiscounts = productDiscounts;
    }

    public void markDeleted() {
        this.isDeleted = true;
        this.deletedAt = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscountMethod)) return false;
        DiscountMethod that = (DiscountMethod) o;
        return Objects.equals(discountMethodId, that.discountMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discountMethodId);
    }

    @Override
    public String toString() {
        return "DiscountMethod{" +
                "discountMethodId=" + discountMethodId +
                ", methodName='" + methodName + '\'' +
                ", isPercentage=" + isPercentage +
                ", maxDiscountValue=" + maxDiscountValue +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
