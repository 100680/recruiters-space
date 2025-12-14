package com.ebuy.payment.dto.request;

import jakarta.validation.constraints.*;


import java.math.BigDecimal;


public class PaymentMethodTypeRequest {

    @NotBlank(message = "Method type is required")
    @Size(max = 50, message = "Method type cannot exceed 50 characters")
    private String methodType;

    @NotBlank(message = "Method name is required")
    @Size(max = 50, message = "Method name cannot exceed 50 characters")
    private String methodName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private Short displayOrder;

    @NotNull(message = "Requires card details flag is required")
    private Boolean requiresCardDetails;

    @NotNull(message = "Requires bank details flag is required")
    private Boolean requiresBankDetails;

    @DecimalMin(value = "0.00", message = "Processing fee percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Processing fee percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal processingFeePercentage;

    @DecimalMin(value = "0.00", message = "Minimum amount cannot be negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal minAmount;

    @Digits(integer = 8, fraction = 2)
    private BigDecimal maxAmount;

    // Getters and Setters

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
}