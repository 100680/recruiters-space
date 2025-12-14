package com.ebuy.payment.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentStatusHistoryResponse {

    private Long paymentStatusHistoryId;
    private Long paymentId;
    private String previousStatusCode;
    private String previousStatusName;
    private String newStatusCode;
    private String newStatusName;
    private OffsetDateTime changedAt;
    private String changedBy;
    private String reason;
    private UUID correlationId;

    // Constructors

    public PaymentStatusHistoryResponse() {
    }

    // Getters and Setters

    public Long getPaymentStatusHistoryId() {
        return paymentStatusHistoryId;
    }

    public void setPaymentStatusHistoryId(Long paymentStatusHistoryId) {
        this.paymentStatusHistoryId = paymentStatusHistoryId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getPreviousStatusCode() {
        return previousStatusCode;
    }

    public void setPreviousStatusCode(String previousStatusCode) {
        this.previousStatusCode = previousStatusCode;
    }

    public String getPreviousStatusName() {
        return previousStatusName;
    }

    public void setPreviousStatusName(String previousStatusName) {
        this.previousStatusName = previousStatusName;
    }

    public String getNewStatusCode() {
        return newStatusCode;
    }

    public void setNewStatusCode(String newStatusCode) {
        this.newStatusCode = newStatusCode;
    }

    public String getNewStatusName() {
        return newStatusName;
    }

    public void setNewStatusName(String newStatusName) {
        this.newStatusName = newStatusName;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }
}