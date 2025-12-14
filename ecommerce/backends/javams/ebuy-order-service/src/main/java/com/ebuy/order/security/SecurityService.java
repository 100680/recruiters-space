package com.ebuy.order.security;

import com.ebuy.order.entity.Order;
import com.ebuy.order.repository.OrderRepository;
import com.ebuy.order.enums.OrderStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * High-performance security service for order and order item operations.
 * Implements caching and optimized authorization checks for scalability.
 */
@Service
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    // Cache for authorization decisions to improve performance
    private final ConcurrentMap<String, AuthorizationResult> authorizationCache = new ConcurrentHashMap<>();

    // Configurable roles and permissions
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String ORDER_MANAGER_ROLE = "ROLE_ORDER_MANAGER";
    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";

    // Modifiable order statuses
    private static final Set<OrderStatus> MODIFIABLE_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING
    );

    // Cache TTL in minutes
    private static final int CACHE_TTL_MINUTES = 5;

    private final OrderRepository orderRepository;

    @Autowired
    public SecurityService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Checks if the current user can modify the specified order.
     */
    public boolean canModifyOrder(Long orderId, Long currentUserId) {
        logger.debug("Checking modify permission for order: {} by user: {}", orderId, currentUserId);

        String cacheKey = String.format("modify_%d_%d", orderId, currentUserId);
        AuthorizationResult cachedResult = authorizationCache.get(cacheKey);

        if (cachedResult != null && !cachedResult.isExpired()) {
            logger.debug("Using cached authorization result for order modify check");
            return cachedResult.isAuthorized();
        }

        boolean canModify = performOrderModifyCheck(orderId, currentUserId);

        // Cache the result
        authorizationCache.put(cacheKey, new AuthorizationResult(canModify,
                OffsetDateTime.now().plus(CACHE_TTL_MINUTES, ChronoUnit.MINUTES)));

        return canModify;
    }

    /**
     * Checks if the specified user has access to read the order.
     */
    @Cacheable(value = "orderAccess", key = "#orderId + '_' + #username")
    public boolean hasOrderAccess(Long orderId, String username) {
        logger.debug("Checking order access for order: {} by user: {}", orderId, username);

        String cacheKey = String.format("access_%d_%s", orderId, username);
        AuthorizationResult cachedResult = authorizationCache.get(cacheKey);

        if (cachedResult != null && !cachedResult.isExpired()) {
            logger.debug("Using cached authorization result for order access check");
            return cachedResult.isAuthorized();
        }

        boolean hasAccess = performOrderAccessCheck(orderId, username);

        // Cache the result
        authorizationCache.put(cacheKey, new AuthorizationResult(hasAccess,
                OffsetDateTime.now().plus(CACHE_TTL_MINUTES, ChronoUnit.MINUTES)));

        return hasAccess;
    }

    /**
     * Checks if the current user is an admin.
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ADMIN_ROLE::equals);
    }

    /**
     * Checks if the current user is an order manager.
     */
    public boolean isCurrentUserOrderManager() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> ORDER_MANAGER_ROLE.equals(authority) || ADMIN_ROLE.equals(authority));
    }

    /**
     * Gets the current user ID from the security context.
     */
    public Long getCurrentUserId() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        // Assuming the principal contains user ID information
        // This implementation depends on your UserDetails implementation
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getUserId();
        }

        return null;
    }

    /**
     * Gets the current username from the security context.
     */
    public String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    /**
     * Validates that the user can perform bulk operations.
     */
    public boolean canPerformBulkOperations(Long userId) {
        logger.debug("Checking bulk operation permission for user: {}", userId);

        // Admin and order managers can perform bulk operations
        if (isCurrentUserAdmin() || isCurrentUserOrderManager()) {
            return true;
        }

        // Regular customers have limited bulk operation capabilities
        return false;
    }

    /**
     * Checks if the order is in a modifiable state.
     */
    public boolean isOrderModifiable(Long orderId) {
        logger.debug("Checking if order is modifiable: {}", orderId);

        String cacheKey = String.format("modifiable_%d", orderId);
        AuthorizationResult cachedResult = authorizationCache.get(cacheKey);

        if (cachedResult != null && !cachedResult.isExpired()) {
            return cachedResult.isAuthorized();
        }

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false;
            }

            boolean isModifiable = MODIFIABLE_STATUSES.contains(order.getStatus()) &&
                    !order.getIsDeleted() &&
                    isWithinModificationTimeLimit(order.getCreatedAt());

            // Cache the result
            authorizationCache.put(cacheKey, new AuthorizationResult(isModifiable,
                    OffsetDateTime.now().plus(CACHE_TTL_MINUTES, ChronoUnit.MINUTES)));

            return isModifiable;

        } catch (Exception e) {
            logger.error("Error checking order modifiability for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * Validates API rate limits for the current user.
     */
    public boolean isWithinRateLimit(String operation, Long userId) {
        // Implementation would depend on your rate limiting strategy
        // This could integrate with Redis, Hazelcast, or other rate limiting solutions

        String rateLimitKey = String.format("rate_limit_%s_%d", operation, userId);

        // Basic implementation - in production, use proper rate limiting
        // such as token bucket or sliding window
        logger.debug("Checking rate limit for operation: {} by user: {}", operation, userId);

        return true; // Simplified for this example
    }

    /**
     * Clears the authorization cache for a specific user.
     */
    public void clearUserAuthorizationCache(Long userId) {
        authorizationCache.entrySet().removeIf(entry ->
                entry.getKey().contains("_" + userId + "_") || entry.getKey().endsWith("_" + userId));

        logger.info("Cleared authorization cache for user: {}", userId);
    }

    /**
     * Clears the entire authorization cache.
     */
    public void clearAuthorizationCache() {
        authorizationCache.clear();
        logger.info("Cleared all authorization cache");
    }

    // Private helper methods

    private boolean performOrderModifyCheck(Long orderId, Long currentUserId) {
        try {
            // Admin and order managers can modify any order
            if (isCurrentUserAdmin() || isCurrentUserOrderManager()) {
                return isOrderModifiable(orderId);
            }

            // Regular users can only modify their own orders
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false;
            }

            // Check if user owns the order
            if (!order.getUserId().equals(currentUserId)) {
                logger.warn("User {} attempted to modify order {} owned by user {}",
                        currentUserId, orderId, order.getUserId());
                return false;
            }

            return isOrderModifiable(orderId);

        } catch (Exception e) {
            logger.error("Error performing order modify check for order: {} by user: {}",
                    orderId, currentUserId, e);
            return false;
        }
    }

    private boolean performOrderAccessCheck(Long orderId, String username) {
        try {
            // Admin and order managers can access any order
            if (isCurrentUserAdmin() || isCurrentUserOrderManager()) {
                return true;
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false;
            }

            // Check if user owns the order or is associated with it
            return order.getUsername().equals(username) ||
                    isUserAssociatedWithOrder(orderId, username);

        } catch (Exception e) {
            logger.error("Error performing order access check for order: {} by user: {}",
                    orderId, username, e);
            return false;
        }
    }

    private boolean isUserAssociatedWithOrder(Long orderId, String username) {
        // This could check for shared orders, family accounts, business accounts, etc.
        // For now, return false - implement based on your business requirements
        return false;
    }

    private boolean isWithinModificationTimeLimit(OffsetDateTime createdAt) {
        // Allow modifications within 24 hours of order creation
        // This can be configurable based on business requirements
        OffsetDateTime cutoff = OffsetDateTime.now().minus(24, ChronoUnit.HOURS);
        return createdAt.isAfter(cutoff);
    }

    private Authentication getCurrentAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            logger.debug("No authentication context available");
            return null;
        }
    }

    // Inner classes

    /**
     * Represents a cached authorization result with expiration.
     */
    private static class AuthorizationResult {
        private final boolean authorized;
        private final OffsetDateTime expiresAt;

        public AuthorizationResult(boolean authorized, OffsetDateTime expiresAt) {
            this.authorized = authorized;
            this.expiresAt = expiresAt;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public boolean isExpired() {
            return OffsetDateTime.now().isAfter(expiresAt);
        }
    }

    /**
     * Custom UserPrincipal interface - implement based on your user details structure.
     */
    public interface UserPrincipal {
        Long getUserId();
        String getUsername();
        List<String> getRoles();
    }
}