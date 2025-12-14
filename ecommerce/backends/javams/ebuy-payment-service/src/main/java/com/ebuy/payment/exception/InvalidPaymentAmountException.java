package com.ebuy.payment.exception;

import java.math.BigDecimal;

public class InvalidPaymentAmountException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final BigDecimal amount;

    public InvalidPaymentAmountException(BigDecimal amount) {
        super(String.format("Invalid payment amount: %s. Amount must be greater than zero.", amount));
        this.amount = amount;
    }

    public InvalidPaymentAmountException(String message) {
        super(message);
        this.amount = null;
    }

    public InvalidPaymentAmountException(String message, Throwable cause) {
        super(message, cause);
        this.amount = null;
    }

    public InvalidPaymentAmountException(BigDecimal amount, String customMessage) {
        super(String.format("%s Invalid amount: %s", customMessage, amount));
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}