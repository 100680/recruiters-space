package com.ebuy.cart.dto.response;

import java.util.List;

public class CartResponse {
    private Long userId;
    private String sessionId;
    private List<CartItemResponse> items;
    private Integer totalItems;

    public CartResponse() {}

    public CartResponse(Long userId, String sessionId, List<CartItemResponse> items, Integer totalItems) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.items = items;
        this.totalItems = totalItems;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
}