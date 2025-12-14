package com.ebuy.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePaymentStatusRequest {

    @NotBlank(message = "Status code is required")
    @Size(max = 20, message = "Status code cannot exceed 20 characters")
    private String statusCode;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 100, message = "Changed by cannot exceed 100 characters")
    private String changedBy;

    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    private String failureReason;

    private String gatewayResponse;

    // Constructors

    public UpdatePaymentStatusRequest() {
    }

    public UpdatePaymentStatusRequest(String statusCode, String reason, String changedBy, String failureReason, String gatewayResponse) {
        this.statusCode = statusCode;
        this.reason = reason;
        this.changedBy = changedBy;
        this.failureReason = failureReason;
        this.gatewayResponse = gatewayResponse;
    }

    // Getters and Setters

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
}