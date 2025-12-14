package com.ebuy.payment.exception;


public class InvalidPaymentStatusException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public InvalidPaymentStatusException(String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition from '%s' to '%s'", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidPaymentStatusException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }
}