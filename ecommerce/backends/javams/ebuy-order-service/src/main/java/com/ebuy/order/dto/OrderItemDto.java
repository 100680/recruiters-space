package com.ebuy.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Data Transfer Object for OrderItem with validation constraints.
 */
public class OrderItemDto {

    private Long orderItemId;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    private Long discountMethodId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount value must be non-negative")
    private BigDecimal discountValue;

    @NotNull(message = "Final price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Final price must be non-negative")
    private BigDecimal finalPrice;

    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
    private OffsetDateTime deletedAt;

    @NotNull(message = "isDeleted cannot be null")
    private Boolean isDeleted = false;

    @NotNull(message = "Row version cannot be null")
    private Long rowVersion = 1L;

    // Constructors
    public OrderItemDto() {
    }

    public OrderItemDto(Long orderItemId,
                        Long orderId,
                        Long productId,
                        Integer quantity,
                        BigDecimal price,
                        Long discountMethodId,
                        BigDecimal discountValue,
                        BigDecimal finalPrice,
                        OffsetDateTime createdAt,
                        OffsetDateTime modifiedAt,
                        OffsetDateTime deletedAt,
                        Boolean isDeleted,
                        Long rowVersion) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.discountMethodId = discountMethodId;
        this.discountValue = discountValue;
        this.finalPrice = finalPrice;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.deletedAt = deletedAt;
        this.isDeleted = isDeleted;
        this.rowVersion = rowVersion;
    }

    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getDiscountMethodId() {
        return discountMethodId;
    }

    public void setDiscountMethodId(Long discountMethodId) {
        this.discountMethodId = discountMethodId;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
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

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }

    @Override
    public String toString() {
        return "OrderItemDto{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", finalPrice=" + finalPrice +
                ", rowVersion=" + rowVersion +
                '}';
    }
}
