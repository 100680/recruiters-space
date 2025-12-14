package com.ebuy.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentMethodTypeResponse {

    private Long paymentMethodTypeId;
    private String methodType;
    private String methodName;
    private String description;
    private Boolean isActive;
    private Short displayOrder;
    private Boolean requiresCardDetails;
    private Boolean requiresBankDetails;
    private BigDecimal processingFeePercentage;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
    private Long rowVersion;

    // Constructors

    public PaymentMethodTypeResponse() {
    }

    // Getters and Setters

    public Long getPaymentMethodTypeId() {
        return paymentMethodTypeId;
    }

    public void setPaymentMethodTypeId(Long paymentMethodTypeId) {
        this.paymentMethodTypeId = paymentMethodTypeId;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Short getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Short displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getRequiresCardDetails() {
        return requiresCardDetails;
    }

    public void setRequiresCardDetails(Boolean requiresCardDetails) {
        this.requiresCardDetails = requiresCardDetails;
    }

    public Boolean getRequiresBankDetails() {
        return requiresBankDetails;
    }

    public void setRequiresBankDetails(Boolean requiresBankDetails) {
        this.requiresBankDetails = requiresBankDetails;
    }

    public BigDecimal getProcessingFeePercentage() {
        return processingFeePercentage;
    }

    public void setProcessingFeePercentage(BigDecimal processingFeePercentage) {
        this.processingFeePercentage = processingFeePercentage;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
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