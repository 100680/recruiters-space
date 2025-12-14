package com.ebuy.user.dto.response;

import com.ebuy.user.entity.User;

import java.time.ZonedDateTime;

public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String address;
    private String phone;
    private ZonedDateTime createdAt;
    private ZonedDateTime modifiedAt;

    public UserResponse() {}

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.phone = user.getPhone();
        this.createdAt = user.getCreatedAt();
        this.modifiedAt = user.getModifiedAt();
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(ZonedDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}