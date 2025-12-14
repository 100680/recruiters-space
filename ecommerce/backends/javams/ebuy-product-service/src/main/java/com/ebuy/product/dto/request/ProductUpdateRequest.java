package com.ebuy.product.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductUpdateRequest {

    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @DecimalMax(value = "999999999999.9999", message = "Price exceeds maximum allowed value")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private Long categoryId;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String imageUrl;

    @NotNull(message = "Row version is required for optimistic locking")
    private Long rowVersion;

    // Constructors
    public ProductUpdateRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }
}