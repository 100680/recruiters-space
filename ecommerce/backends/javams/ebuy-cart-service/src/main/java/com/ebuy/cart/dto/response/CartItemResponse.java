package com.ebuy.cart.dto.response;

import java.time.Instant;

public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private Integer quantity;
    private Instant createdAt;
    private Instant modifiedAt;
    private Long rowVersion;

    public CartItemResponse() {}

    public CartItemResponse(Long cartItemId, Long productId, Integer quantity,
                            Instant createdAt, Instant modifiedAt, Long rowVersion) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.rowVersion = rowVersion;
    }

    // Getters and setters
    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Instant modifiedAt) { this.modifiedAt = modifiedAt; }

    public Long getRowVersion() { return rowVersion; }
    public void setRowVersion(Long rowVersion) { this.rowVersion = rowVersion; }
}