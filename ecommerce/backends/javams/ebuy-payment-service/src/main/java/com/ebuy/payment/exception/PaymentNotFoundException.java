package com.ebuy.payment.exception;

/**
 * Exception thrown when payment is not found
 */
public class PaymentNotFoundException extends RuntimeException {

    private Long paymentId;

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found with ID: " + paymentId);
        this.paymentId = paymentId;
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public Long getPaymentId() {
        return paymentId;
    }
}
