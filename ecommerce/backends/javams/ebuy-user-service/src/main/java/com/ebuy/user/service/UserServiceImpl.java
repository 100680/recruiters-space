package com.ebuy.user.service;

import com.ebuy.user.dto.request.CreateUserRequest;
import com.ebuy.user.dto.request.UpdateUserRequest;
import com.ebuy.user.dto.response.UserResponse;
import com.ebuy.user.entity.User;
import com.ebuy.user.exception.UserAlreadyExistsException;
import com.ebuy.user.exception.UserNotFoundException;
import com.ebuy.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @CachePut(value = "users", key = "#result.userId")
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getUserId());

        return new UserResponse(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserById(Long userId) {
        logger.info("Fetching user with ID: {}", userId);
        
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        return new UserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);
        
        User user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        return new UserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        
        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted())
                .collect(Collectors.toList());
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @CachePut(value = "users", key = "#userId")
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setName(request.getName());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", updatedUser.getUserId());

        return new UserResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsDeleted(true);
        userRepository.save(user);
        
        logger.info("User soft deleted successfully with ID: {}", userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email);
    }
}