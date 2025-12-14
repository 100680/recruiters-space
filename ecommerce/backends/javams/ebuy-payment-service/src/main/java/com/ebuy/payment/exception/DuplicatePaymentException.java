package com.ebuy.payment.exception;

import java.util.UUID;

public class DuplicatePaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final UUID correlationId;
    private final Long orderId;

    public DuplicatePaymentException(UUID correlationId) {
        super(String.format("Duplicate payment detected with correlation ID: %s", correlationId));
        this.correlationId = correlationId;
        this.orderId = null;
    }

    public DuplicatePaymentException(Long orderId, String message) {
        super(String.format("Duplicate payment for order ID %d: %s", orderId, message));
        this.orderId = orderId;
        this.correlationId = null;
    }

    public DuplicatePaymentException(String message) {
        super(message);
        this.correlationId = null;
        this.orderId = null;
    }

    public DuplicatePaymentException(String message, Throwable cause) {
        super(message, cause);
        this.correlationId = null;
        this.orderId = null;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public Long getOrderId() {
        return orderId;
    }
}