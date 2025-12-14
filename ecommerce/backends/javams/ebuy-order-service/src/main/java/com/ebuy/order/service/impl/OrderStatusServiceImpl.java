package com.ebuy.order.service.impl;

import com.ebuy.order.dto.OrderStatusDto;
import com.ebuy.order.entity.OrderStatus;
import com.ebuy.order.exception.*;
import com.ebuy.order.repository.OrderStatusRepository;
import com.ebuy.order.service.OrderStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderStatusServiceImpl implements OrderStatusService {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusServiceImpl.class);

    private static final String CACHE_ORDER_STATUSES = "orderStatuses";
    private static final String CACHE_ORDER_STATUS_BY_ID = "orderStatusById";
    private static final String CACHE_ORDER_STATUS_BY_NAME = "orderStatusByName";
    private static final String CACHE_ACTIVE_ORDER_STATUSES = "activeOrderStatuses";
    private static final String CACHE_DEFAULT_ORDER_STATUS = "defaultOrderStatus";

    // System status names that cannot be deleted
    private static final Set<String> SYSTEM_STATUSES = Set.of(
            "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED",
            "CANCELLED", "REFUNDED", "RETURNED"
    );

    // Valid status transitions map
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "PENDING", Set.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", Set.of("PROCESSING", "CANCELLED"),
            "PROCESSING", Set.of("SHIPPED", "CANCELLED"),
            "SHIPPED", Set.of("DELIVERED", "RETURNED"),
            "DELIVERED", Set.of("RETURNED", "REFUNDED"),
            "CANCELLED", Set.of("REFUNDED"),
            "RETURNED", Set.of("REFUNDED"),
            "REFUNDED", Collections.emptySet()
    );

    private final OrderStatusRepository orderStatusRepository;

    @Autowired
    public OrderStatusServiceImpl(OrderStatusRepository orderStatusRepository) {
        this.orderStatusRepository = orderStatusRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = {CACHE_ORDER_STATUSES, CACHE_ACTIVE_ORDER_STATUSES}, allEntries = true)
    public OrderStatusDto createOrderStatus(OrderStatusDto orderStatusDto, Long currentUserId) {
        log.info("Creating order status with name: {} by user: {}", orderStatusDto.getStatusName(), currentUserId);

        validateOrderStatusData(orderStatusDto);
        validateCurrentUser(currentUserId);

        String statusName = orderStatusDto.getStatusName().trim();

        // Check if status already exists
        if (orderStatusRepository.existsByStatusNameIgnoreCaseAndIsDeletedFalse(statusName)) {
            log.warn("Order status already exists with name: {}", statusName);
            throw new OrderStatusAlreadyExistsException("Order status with name '" + statusName + "' already exists");
        }

        try {
            OrderStatus orderStatus = new OrderStatus(statusName);
            OrderStatus savedStatus = orderStatusRepository.save(orderStatus);

            log.info("Successfully created order status with ID: {} and name: {}", savedStatus.getStatusId(), savedStatus.getStatusName());
            return mapToDto(savedStatus);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating order status: {}", e.getMessage());
            throw new OrderStatusCreationException("Failed to create order status due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating order status: {}", e.getMessage(), e);
            throw new OrderStatusCreationException("Failed to create order status: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = CACHE_ORDER_STATUS_BY_ID, key = "#statusId")
    public OrderStatusDto getOrderStatusById(Long statusId) {
        log.debug("Retrieving order status by ID: {}", statusId);

        if (statusId == null || statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive number");
        }

        try {
            OrderStatus orderStatus = orderStatusRepository.findById(statusId)
                    .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + statusId));

            return mapToDto(orderStatus);

        } catch (OrderStatusNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving order status by ID {}: {}", statusId, e.getMessage(), e);
            throw new OrderStatusRetrievalException("Failed to retrieve order status with ID: " + statusId, e);
        }
    }

    @Override
    @Cacheable(value = CACHE_ORDER_STATUS_BY_NAME, key = "#statusName?.toLowerCase()")
    public OrderStatusDto getOrderStatusByName(String statusName) {
        log.debug("Retrieving order status by name: {}", statusName);

        if (!StringUtils.hasText(statusName)) {
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }

        try {
            OrderStatus orderStatus = orderStatusRepository.findByStatusNameIgnoreCase(statusName.trim())
                    .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with name: " + statusName));

            return mapToDto(orderStatus);

        } catch (OrderStatusNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving order status by name {}: {}", statusName, e.getMessage(), e);
            throw new OrderStatusRetrievalException("Failed to retrieve order status with name: " + statusName, e);
        }
    }

    @Override
    @Cacheable(value = CACHE_ORDER_STATUSES)
    public List<OrderStatusDto> getAllOrderStatuses() {
        log.debug("Retrieving all order statuses");

        try {
            List<OrderStatus> orderStatuses = orderStatusRepository.findAll();
            return orderStatuses.stream()
                    .map(this::mapToDto)
                    .sorted(Comparator.comparing(OrderStatusDto::getStatusName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving all order statuses: {}", e.getMessage(), e);
            throw new OrderStatusRetrievalException("Failed to retrieve order statuses", e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = {CACHE_ORDER_STATUSES, CACHE_ORDER_STATUS_BY_ID, CACHE_ORDER_STATUS_BY_NAME, CACHE_ACTIVE_ORDER_STATUSES}, allEntries = true)
    public OrderStatusDto updateOrderStatus(Long statusId, OrderStatusDto orderStatusDto, Long currentUserId) {
        log.info("Updating order status with ID: {} by user: {}", statusId, currentUserId);

        validateOrderStatusData(orderStatusDto);
        validateCurrentUser(currentUserId);

        if (statusId == null || statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive number");
        }

        String newStatusName = orderStatusDto.getStatusName().trim();

        try {
            OrderStatus existingStatus = orderStatusRepository.findById(statusId)
                    .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + statusId));

            // Check if new name already exists (excluding current status)
            Optional<OrderStatus> duplicateStatus = orderStatusRepository.findByStatusNameIgnoreCase(newStatusName);
            if (duplicateStatus.isPresent() && !duplicateStatus.get().getStatusId().equals(statusId)) {
                log.warn("Order status already exists with name: {}", newStatusName);
                throw new OrderStatusAlreadyExistsException("Order status with name '" + newStatusName + "' already exists");
            }

            existingStatus.setStatusName(newStatusName);
            if (orderStatusDto.getRowVersion() != null) {
                existingStatus.setRowVersion(orderStatusDto.getRowVersion());
            }

            OrderStatus updatedStatus = orderStatusRepository.save(existingStatus);

            log.info("Successfully updated order status with ID: {}", updatedStatus.getStatusId());
            return mapToDto(updatedStatus);

        } catch (OrderStatusNotFoundException | OrderStatusAlreadyExistsException e) {
            throw e;
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while updating order status with ID: {}", statusId);
            throw new OrderStatusUpdateException("Order status was modified by another user. Please refresh and try again", e);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating order status: {}", e.getMessage());
            throw new OrderStatusUpdateException("Failed to update order status due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error while updating order status with ID {}: {}", statusId, e.getMessage(), e);
            throw new OrderStatusUpdateException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = {CACHE_ORDER_STATUSES, CACHE_ORDER_STATUS_BY_ID, CACHE_ORDER_STATUS_BY_NAME, CACHE_ACTIVE_ORDER_STATUSES}, allEntries = true)
    public void deleteOrderStatus(Long statusId, Long currentUserId) {
        log.info("Deleting order status with ID: {} by user: {}", statusId, currentUserId);

        validateCurrentUser(currentUserId);

        if (statusId == null || statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive number");
        }

        try {
            OrderStatus orderStatus = orderStatusRepository.findById(statusId)
                    .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with ID: " + statusId));

            // Check if it's a system status
            if (SYSTEM_STATUSES.contains(orderStatus.getStatusName().toUpperCase())) {
                log.warn("Attempt to delete system status: {}", orderStatus.getStatusName());
                throw new OrderStatusDeletionException("Cannot delete system status: " + orderStatus.getStatusName());
            }

            // Soft delete
            orderStatus.setIsDeleted(true);
            orderStatus.setDeletedAt(OffsetDateTime.now());
            orderStatusRepository.save(orderStatus);

            log.info("Successfully deleted order status with ID: {}", statusId);

        } catch (OrderStatusNotFoundException | OrderStatusDeletionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting order status with ID {}: {}", statusId, e.getMessage(), e);
            throw new OrderStatusDeletionException("Failed to delete order status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByStatusName(String statusName) {
        if (!StringUtils.hasText(statusName)) {
            return false;
        }

        try {
            return orderStatusRepository.existsByStatusNameIgnoreCaseAndIsDeletedFalse(statusName.trim());
        } catch (Exception e) {
            log.error("Error checking if order status exists by name {}: {}", statusName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Cacheable(value = CACHE_ACTIVE_ORDER_STATUSES)
    public List<OrderStatusDto> getActiveOrderStatuses() {
        log.debug("Retrieving active order statuses");

        try {
            List<OrderStatus> allStatuses = orderStatusRepository.findAll();

            // Filter active statuses (exclude CANCELLED, REFUNDED, RETURNED for new orders)
            Set<String> inactiveStatuses = Set.of("CANCELLED", "REFUNDED", "RETURNED");

            return allStatuses.stream()
                    .filter(status -> !inactiveStatuses.contains(status.getStatusName().toUpperCase()))
                    .map(this::mapToDto)
                    .sorted(Comparator.comparing(OrderStatusDto::getStatusName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving active order statuses: {}", e.getMessage(), e);
            throw new OrderStatusRetrievalException("Failed to retrieve active order statuses", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_DEFAULT_ORDER_STATUS)
    public OrderStatusDto getDefaultOrderStatus() {
        log.debug("Retrieving default order status");

        try {
            // Default status is typically "PENDING"
            OrderStatus defaultStatus = orderStatusRepository.findByStatusNameIgnoreCase("PENDING")
                    .orElseThrow(() -> new OrderStatusNotFoundException("Default order status 'PENDING' not found"));

            return mapToDto(defaultStatus);

        } catch (OrderStatusNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving default order status: {}", e.getMessage(), e);
            throw new OrderStatusRetrievalException("Failed to retrieve default order status", e);
        }
    }

    @Override
    public boolean isValidStatusTransition(Long fromStatusId, Long toStatusId) {
        if (fromStatusId == null || toStatusId == null) {
            return false;
        }

        if (fromStatusId.equals(toStatusId)) {
            return true; // Same status transition is always valid
        }

        try {
            OrderStatus fromStatus = orderStatusRepository.findById(fromStatusId).orElse(null);
            OrderStatus toStatus = orderStatusRepository.findById(toStatusId).orElse(null);

            if (fromStatus == null || toStatus == null) {
                return false;
            }

            String fromStatusName = fromStatus.getStatusName().toUpperCase();
            String toStatusName = toStatus.getStatusName().toUpperCase();

            Set<String> validTransitions = VALID_TRANSITIONS.get(fromStatusName);
            return validTransitions != null && validTransitions.contains(toStatusName);

        } catch (Exception e) {
            log.error("Error validating status transition from {} to {}: {}", fromStatusId, toStatusId, e.getMessage(), e);
            return false;
        }
    }

    private void validateOrderStatusData(OrderStatusDto orderStatusDto) {
        if (orderStatusDto == null) {
            throw new InvalidOrderStatusDataException("Order status data cannot be null");
        }

        if (!StringUtils.hasText(orderStatusDto.getStatusName())) {
            throw new InvalidOrderStatusDataException("Order status name cannot be null or empty");
        }

        String statusName = orderStatusDto.getStatusName().trim();
        if (statusName.length() > 50) {
            throw new InvalidOrderStatusDataException("Order status name cannot exceed 50 characters");
        }

        // Validate status name format (alphanumeric and underscores only)
        if (!statusName.matches("^[A-Za-z0-9_\\s]+$")) {
            throw new InvalidOrderStatusDataException("Order status name can only contain letters, numbers, underscores and spaces");
        }
    }

    private void validateCurrentUser(Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            throw new IllegalArgumentException("Current user ID must be a positive number");
        }
    }

    private OrderStatusDto mapToDto(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }

        OrderStatusDto dto = new OrderStatusDto();
        dto.setStatusId(orderStatus.getStatusId());
        dto.setStatusName(orderStatus.getStatusName());
        dto.setCreatedAt(orderStatus.getCreatedAt());
        dto.setModifiedAt(orderStatus.getModifiedAt());
        dto.setRowVersion(orderStatus.getRowVersion());

        return dto;
    }
}