package com.ebuy.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private Long paymentMethodTypeId;
    private Short paymentStatusId;
    private Short currencyCodeId;
    private OffsetDateTime paymentDate;
    private BigDecimal amount;
    private BigDecimal processingFee;
    private BigDecimal netAmount;
    private String transactionReference;
    private UUID correlationId;
    private String serviceOrigin;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
    private Long rowVersion;

    // Constructors

    public PaymentResponse() {
    }

    // Getters and Setters

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentMethodTypeId() {
        return paymentMethodTypeId;
    }

    public void setPaymentMethodTypeId(Long paymentMethodTypeId) {
        this.paymentMethodTypeId = paymentMethodTypeId;
    }

    public Short getPaymentStatusId() {
        return paymentStatusId;
    }

    public void setPaymentStatusId(Short paymentStatusId) {
        this.paymentStatusId = paymentStatusId;
    }

    public Short getCurrencyCodeId() {
        return currencyCodeId;
    }

    public void setCurrencyCodeId(Short currencyCodeId) {
        this.currencyCodeId = currencyCodeId;
    }

    public OffsetDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(OffsetDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public String getServiceOrigin() {
        return serviceOrigin;
    }

    public void setServiceOrigin(String serviceOrigin) {
        this.serviceOrigin = serviceOrigin;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(OffsetDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }
}