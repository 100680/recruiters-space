package com.ebuy.order.enums;

/**
 * Audit action enumeration for tracking different types of operations.
 */
public enum AuditAction {
    CREATE("Create", "Entity creation"),
    UPDATE("Update", "Entity modification"),
    DELETE("Delete", "Entity deletion"),
    READ("Read", "Entity access"),
    BULK_OPERATION("Bulk Operation", "Bulk operation"),
    SECURITY_VIOLATION("Security Violation", "Security violation detected"),
    PERFORMANCE_METRIC("Performance Metric", "Performance measurement");

    private final String displayName;
    private final String description;

    AuditAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
