package com.ebuy.order.audit;

import com.ebuy.order.dto.OrderItemDto;
import com.ebuy.order.entity.AuditLog;
import com.ebuy.order.repository.AuditLogRepository;
import com.ebuy.order.enums.AuditAction;
import com.ebuy.order.enums.AuditLevel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * High-performance audit service for tracking order item operations.
 * Designed for high throughput with asynchronous processing and batch operations.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Value("${ebuy.audit.enabled:true}")
    private boolean auditEnabled;

    @Value("${ebuy.audit.async-enabled:true}")
    private boolean asyncAuditEnabled;

    @Value("${ebuy.audit.retention-days:90}")
    private Integer auditRetentionDays;

    @Value("${ebuy.audit.batch-size:1000}")
    private Integer auditBatchSize;

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository,
                        ObjectMapper objectMapper,
                        HttpServletRequest httpServletRequest) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Logs order item creation event.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logOrderItemCreation(OrderItemDto orderItemDto, Long userId) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Logging order item creation: {}", orderItemDto.getOrderItemId());

        Map<String, Object> auditData = createBaseAuditData(orderItemDto);
        auditData.put("action", "ORDER_ITEM_CREATED");
        auditData.put("orderId", orderItemDto.getOrderId());
        auditData.put("productId", orderItemDto.getProductId());
        auditData.put("quantity", orderItemDto.getQuantity());
        auditData.put("finalPrice", orderItemDto.getFinalPrice());

        return createAuditLogAsync(
                AuditAction.CREATE,
                AuditLevel.INFO,
                "OrderItem",
                orderItemDto.getOrderItemId(),
                userId,
                "Order item created successfully",
                auditData
        );
    }

    /**
     * Logs order item update event.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logOrderItemUpdate(OrderItemDto orderItemDto, Long userId) {
        return logOrderItemUpdate(orderItemDto, userId, null);
    }

    /**
     * Logs order item update event with previous values.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logOrderItemUpdate(OrderItemDto orderItemDto, Long userId,
                                                      OrderItemDto previousValues) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Logging order item update: {}", orderItemDto.getOrderItemId());

        Map<String, Object> auditData = createBaseAuditData(orderItemDto);
        auditData.put("action", "ORDER_ITEM_UPDATED");

        if (previousValues != null) {
            auditData.put("previousValues", createPreviousValuesMap(previousValues));
            auditData.put("changes", calculateChanges(previousValues, orderItemDto));
        }

        return createAuditLogAsync(
                AuditAction.UPDATE,
                AuditLevel.INFO,
                "OrderItem",
                orderItemDto.getOrderItemId(),
                userId,
                "Order item updated successfully",
                auditData
        );
    }

    /**
     * Logs order item deletion event.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logOrderItemDeletion(Long orderItemId, Long userId) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Logging order item deletion: {}", orderItemId);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "ORDER_ITEM_DELETED");
        auditData.put("orderItemId", orderItemId);
        auditData.put("timestamp", OffsetDateTime.now());
        auditData.put("ipAddress", getClientIpAddress());
        auditData.put("userAgent", getUserAgent());

        return createAuditLogAsync(
                AuditAction.DELETE,
                AuditLevel.WARNING,
                "OrderItem",
                orderItemId,
                userId,
                "Order item deleted",
                auditData
        );
    }

    /**
     * Logs bulk order item operations.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logBulkOrderItemOperation(String operation,
                                                             List<OrderItemDto> orderItems,
                                                             Long userId,
                                                             boolean success,
                                                             String errorMessage) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Logging bulk order item operation: {} for {} items", operation, orderItems.size());

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "BULK_" + operation.toUpperCase());
        auditData.put("itemCount", orderItems.size());
        auditData.put("success", success);
        auditData.put("timestamp", OffsetDateTime.now());
        auditData.put("ipAddress", getClientIpAddress());
        auditData.put("userAgent", getUserAgent());

        if (!success && errorMessage != null) {
            auditData.put("errorMessage", errorMessage);
        }

        // Log item IDs for reference
        List<Long> itemIds = orderItems.stream()
                .map(OrderItemDto::getOrderItemId)
                .filter(id -> id != null)
                .toList();
        auditData.put("orderItemIds", itemIds);

        AuditLevel level = success ? AuditLevel.INFO : AuditLevel.ERROR;
        String description = String.format("Bulk %s operation for %d items %s",
                operation, orderItems.size(),
                success ? "completed successfully" : "failed");

        return createAuditLogAsync(
                AuditAction.BULK_OPERATION,
                level,
                "OrderItem",
                null,
                userId,
                description,
                auditData
        );
    }

    /**
     * Logs security violations or suspicious activities.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logSecurityViolation(String violationType,
                                                        Long orderItemId,
                                                        Long userId,
                                                        String details) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.warn("Logging security violation: {} for order item: {} by user: {}",
                violationType, orderItemId, userId);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "SECURITY_VIOLATION");
        auditData.put("violationType", violationType);
        auditData.put("orderItemId", orderItemId);
        auditData.put("details", details);
        auditData.put("timestamp", OffsetDateTime.now());
        auditData.put("ipAddress", getClientIpAddress());
        auditData.put("userAgent", getUserAgent());
        auditData.put("sessionId", getSessionId());

        return createAuditLogAsync(
                AuditAction.SECURITY_VIOLATION,
                AuditLevel.CRITICAL,
                "OrderItem",
                orderItemId,
                userId,
                "Security violation detected: " + violationType,
                auditData
        );
    }

    /**
     * Logs performance metrics for monitoring.
     */
    @Async("auditExecutor")
    public CompletableFuture<Void> logPerformanceMetrics(String operation,
                                                         long executionTimeMs,
                                                         Long userId,
                                                         Map<String, Object> additionalMetrics) {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Logging performance metrics for operation: {}", operation);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "PERFORMANCE_METRIC");
        auditData.put("operation", operation);
        auditData.put("executionTimeMs", executionTimeMs);
        auditData.put("timestamp", OffsetDateTime.now());

        if (additionalMetrics != null) {
            auditData.putAll(additionalMetrics);
        }

        AuditLevel level = executionTimeMs > 5000 ? AuditLevel.WARNING : AuditLevel.DEBUG;

        return createAuditLogAsync(
                AuditAction.PERFORMANCE_METRIC,
                level,
                "OrderItem",
                null,
                userId,
                String.format("Operation %s completed in %d ms", operation, executionTimeMs),
                auditData
        );
    }

    /**
     * Retrieves audit logs for a specific order item.
     */
    public List<AuditLog> getAuditLogsForOrderItem(Long orderItemId, int limit) {
        logger.debug("Retrieving audit logs for order item: {}", orderItemId);

        try {
            return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                    "OrderItem", orderItemId, limit);
        } catch (Exception e) {
            logger.error("Error retrieving audit logs for order item: {}", orderItemId, e);
            throw new RuntimeException("Failed to retrieve audit logs", e);
        }
    }

    /**
     * Retrieves audit logs for a specific user.
     */
    public List<AuditLog> getAuditLogsForUser(Long userId, int limit) {
        logger.debug("Retrieving audit logs for user: {}", userId);

        try {
            return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, limit);
        } catch (Exception e) {
            logger.error("Error retrieving audit logs for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve audit logs", e);
        }
    }

    /**
     * Cleans up old audit logs based on retention policy.
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Integer> cleanupOldAuditLogs() {
        if (!auditEnabled) {
            return CompletableFuture.completedFuture(0);
        }

        logger.info("Starting audit log cleanup process");

        try {
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(auditRetentionDays);
            int deletedCount = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);

            logger.info("Cleaned up {} audit log entries older than {}", deletedCount, cutoffDate);
            return CompletableFuture.completedFuture(deletedCount);

        } catch (Exception e) {
            logger.error("Error during audit log cleanup", e);
            throw new RuntimeException("Audit log cleanup failed", e);
        }
    }

    // Private helper methods

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private CompletableFuture<Void> createAuditLogAsync(AuditAction action,
                                                        AuditLevel level,
                                                        String entityType,
                                                        Long entityId,
                                                        Long userId,
                                                        String description,
                                                        Map<String, Object> auditData) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setLevel(level);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setUserId(userId);
            auditLog.setDescription(description);
            auditLog.setAuditData(objectMapper.writeValueAsString(auditData));
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setSessionId(getSessionId());
            auditLog.setCreatedAt(OffsetDateTime.now());

            auditLogRepository.save(auditLog);

            logger.debug("Audit log created successfully for action: {}", action);
            return CompletableFuture.completedFuture(null);

        } catch (JsonProcessingException e) {
            logger.error("Error serializing audit data", e);
            // Create a simplified audit log without the problematic data
            return createSimpleAuditLog(action, level, entityType, entityId, userId, description);
        } catch (Exception e) {
            logger.error("Error creating audit log", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> createSimpleAuditLog(AuditAction action,
                                                         AuditLevel level,
                                                         String entityType,
                                                         Long entityId,
                                                         Long userId,
                                                         String description) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setLevel(level);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setUserId(userId);
            auditLog.setDescription(description + " (simplified due to serialization error)");
            auditLog.setAuditData("{}");
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setCreatedAt(OffsetDateTime.now());

            auditLogRepository.save(auditLog);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Error creating simple audit log", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private Map<String, Object> createBaseAuditData(OrderItemDto orderItemDto) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("orderItemId", orderItemDto.getOrderItemId());
        auditData.put("orderId", orderItemDto.getOrderId());
        auditData.put("productId", orderItemDto.getProductId());
        auditData.put("quantity", orderItemDto.getQuantity());
        auditData.put("price", orderItemDto.getPrice());
        auditData.put("finalPrice", orderItemDto.getFinalPrice());
        auditData.put("rowVersion", orderItemDto.getRowVersion());
        auditData.put("timestamp", OffsetDateTime.now());
        auditData.put("ipAddress", getClientIpAddress());
        auditData.put("userAgent", getUserAgent());

        return auditData;
    }

    private Map<String, Object> createPreviousValuesMap(OrderItemDto previousValues) {
        Map<String, Object> previous = new HashMap<>();
        previous.put("quantity", previousValues.getQuantity());
        previous.put("price", previousValues.getPrice());
        previous.put("discountValue", previousValues.getDiscountValue());
        previous.put("finalPrice", previousValues.getFinalPrice());
        previous.put("rowVersion", previousValues.getRowVersion());

        return previous;
    }

    private Map<String, Object> calculateChanges(OrderItemDto oldValues, OrderItemDto newValues) {
        Map<String, Object> changes = new HashMap<>();

        if (!oldValues.getQuantity().equals(newValues.getQuantity())) {
            changes.put("quantity", Map.of("from", oldValues.getQuantity(), "to", newValues.getQuantity()));
        }

        if (oldValues.getPrice().compareTo(newValues.getPrice()) != 0) {
            changes.put("price", Map.of("from", oldValues.getPrice(), "to", newValues.getPrice()));
        }

        if (!equals(oldValues.getDiscountValue(), newValues.getDiscountValue())) {
            changes.put("discountValue", Map.of("from", oldValues.getDiscountValue(), "to", newValues.getDiscountValue()));
        }

        if (oldValues.getFinalPrice().compareTo(newValues.getFinalPrice()) != 0) {
            changes.put("finalPrice", Map.of("from", oldValues.getFinalPrice(), "to", newValues.getFinalPrice()));
        }

        return changes;
    }

    private boolean equals(Object obj1, Object obj2) {
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

    private String getClientIpAddress() {
        try {
            String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = httpServletRequest.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return httpServletRequest.getRemoteAddr();
        } catch (Exception e) {
            logger.debug("Error getting client IP address", e);
            return "unknown";
        }
    }

    private String getUserAgent() {
        try {
            return httpServletRequest.getHeader("User-Agent");
        } catch (Exception e) {
            logger.debug("Error getting user agent", e);
            return "unknown";
        }
    }

    private String getSessionId() {
        try {
            return httpServletRequest.getSession(false) != null ?
                    httpServletRequest.getSession().getId() : null;
        } catch (Exception e) {
            logger.debug("Error getting session ID", e);
            return null;
        }
    }
}