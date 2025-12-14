package com.ebuy.user.service;

import com.ebuy.user.dto.request.CreateUserRequest;
import com.ebuy.user.dto.request.UpdateUserRequest;
import com.ebuy.user.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long userId);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    void deleteUser(Long userId);
    boolean existsByEmail(String email);
}
