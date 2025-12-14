package com.ebuy.order.enums;

/**
 * Audit level enumeration for categorizing audit entries by importance.
 */
public enum AuditLevel {
    DEBUG("Debug", 0, "Detailed debugging information"),
    INFO("Info", 1, "General information"),
    WARNING("Warning", 2, "Warning condition"),
    ERROR("Error", 3, "Error condition"),
    CRITICAL("Critical", 4, "Critical system condition");

    private final String displayName;
    private final int severity;
    private final String description;

    AuditLevel(String displayName, int severity, String description) {
        this.displayName = displayName;
        this.severity = severity;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHighSeverity() {
        return severity >= ERROR.severity;
    }
}