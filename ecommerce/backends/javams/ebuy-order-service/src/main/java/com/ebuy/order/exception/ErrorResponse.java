package com.ebuy.order.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Standardized error response structure for the eBuy Order Management service.
 * Provides consistent error information across all API endpoints with
 * support for validation errors, tracing, and monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * HTTP status reason phrase
     */
    private String error;

    /**
     * Application-specific error code for categorization
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Request path where the error occurred
     */
    private String path;

    /**
     * Correlation ID for request tracking across services
     */
    private String correlationId;

    /**
     * Trace ID for distributed tracing
     */
    private String traceId;

    /**
     * Validation errors for field-specific validation failures
     * Key: field name, Value: validation error message
     */
    private Map<String, String> validationErrors;

    /**
     * Additional context information (optional)
     */
    private Map<String, Object> details;
}