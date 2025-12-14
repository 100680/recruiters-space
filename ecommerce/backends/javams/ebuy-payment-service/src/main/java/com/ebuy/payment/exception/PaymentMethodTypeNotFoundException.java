package com.ebuy.payment.exception;

public class PaymentMethodTypeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long paymentMethodTypeId;
    private final String methodName;

    public PaymentMethodTypeNotFoundException(Long paymentMethodTypeId) {
        super(String.format("Payment method type not found with ID: %d", paymentMethodTypeId));
        this.paymentMethodTypeId = paymentMethodTypeId;
        this.methodName = null;
    }

    public PaymentMethodTypeNotFoundException(String methodName) {
        super(String.format("Payment method type not found with name: %s", methodName));
        this.methodName = methodName;
        this.paymentMethodTypeId = null;
    }

    public PaymentMethodTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.paymentMethodTypeId = null;
        this.methodName = null;
    }

    public Long getPaymentMethodTypeId() {
        return paymentMethodTypeId;
    }

    public String getMethodName() {
        return methodName;
    }
}