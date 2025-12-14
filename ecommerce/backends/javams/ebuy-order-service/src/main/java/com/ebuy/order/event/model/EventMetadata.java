package com.ebuy.order.event.model;

/**
 * Event metadata for tracking and correlation.
 */
public class EventMetadata {
    private String source;
    private String version;
    private String correlationId;
    private String origin;

    // Constructors
    public EventMetadata() {}

    // Getters and Setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
}