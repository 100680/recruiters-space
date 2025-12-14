package com.ebuy.payment.exception;


import java.util.UUID;

public class PaymentProcessingException extends RuntimeException {

    private final String errorCode;
    private final UUID correlationId;

    public PaymentProcessingException(String message) {
        super(message);
        this.errorCode = "PAYMENT_PROCESSING_ERROR";
        this.correlationId = null;
    }

    public PaymentProcessingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = null;
    }

    public PaymentProcessingException(String message, String errorCode, UUID correlationId) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_PROCESSING_ERROR";
        this.correlationId = null;
    }

    public PaymentProcessingException(String message, Throwable cause, String errorCode, UUID correlationId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }
}
