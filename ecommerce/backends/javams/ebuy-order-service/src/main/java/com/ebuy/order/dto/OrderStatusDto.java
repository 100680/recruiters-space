package com.ebuy.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class OrderStatusDto {

    private Long statusId;

    @NotBlank(message = "Status name cannot be blank")
    @Size(max = 50, message = "Status name cannot exceed 50 characters")
    private String statusName;

    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
    private Long rowVersion;

    // Constructors
    public OrderStatusDto() {}

    public OrderStatusDto(String statusName) {
        this.statusName = statusName;
    }

    // Getters and Setters
    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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

    @Override
    public String toString() {
        return "OrderStatusDto{" +
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                ", rowVersion=" + rowVersion +
                '}';
    }
}