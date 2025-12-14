package com.ebuy.payment.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Payment status enumeration matching the database enum
 */
public enum PaymentStatus {
    PENDING("pending"),
    AUTHORIZED("authorized"),
    CAPTURED("captured"),
    FAILED("failed"),
    REFUNDED("refunded"),
    VOIDED("voided");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}