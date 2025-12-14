package com.ebuy.order.enums;

/**
 * Order status enumeration for order lifecycle management.
 */
public enum OrderStatus {
    PENDING("Pending", "Order is pending confirmation"),
    CONFIRMED("Confirmed", "Order has been confirmed"),
    PROCESSING("Processing", "Order is being processed"),
    SHIPPED("Shipped", "Order has been shipped"),
    DELIVERED("Delivered", "Order has been delivered"),
    CANCELLED("Cancelled", "Order has been cancelled"),
    REFUNDED("Refunded", "Order has been refunded"),
    RETURNED("Returned", "Order has been returned");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isModifiable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED || this == RETURNED;
    }
}
