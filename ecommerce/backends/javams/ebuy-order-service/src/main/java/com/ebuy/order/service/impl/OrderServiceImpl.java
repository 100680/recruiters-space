package com.ebuy.order.service.impl;

import com.ebuy.order.dto.OrderDto;
import com.ebuy.order.entity.Order;
import com.ebuy.order.entity.OrderStatus;
import com.ebuy.order.exception.*;
import com.ebuy.order.mapper.OrderMapper;
import com.ebuy.order.repository.OrderRepository;
import com.ebuy.order.repository.OrderStatusRepository;
import com.ebuy.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Retryable(value = {OptimisticLockingException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public OrderDto createOrder(OrderDto orderDto, Long currentUserId) {
        validateOrderCreation(orderDto, currentUserId);

        Order order = orderMapper.toEntity(orderDto);

        // Set audit fields
        if (currentUserId != null) {
            order.setServiceOrigin("USER_SERVICE");
        }

        // Set default status if not provided
        if (order.getStatus() == null) {
            OrderStatus defaultStatus = getDefaultOrderStatus();
            order.setStatus(defaultStatus);
        }

        // Calculate and validate total amount
        BigDecimal calculatedTotal = calculateTotalAmount(order);
        if (orderDto.getTotalAmount() != null &&
                orderDto.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new InvalidOrderAmountException("Provided total amount does not match calculated amount");
        }
        order.setTotalAmount(calculatedTotal);

        try {
            Order savedOrder = orderRepository.save(order);
            log.info("Order created successfully: orderId={}, userId={}, total={}",
                    savedOrder.getOrderId(), savedOrder.getUserId(), savedOrder.getTotalAmount());
            return orderMapper.toDto(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create order for user: {}", orderDto.getUserId(), e);
            throw new OrderCreationException("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDto(order);
    }

    @Override
    @Cacheable(value = "orders-with-items", key = "#orderId", unless = "#result == null")
    public OrderDto getOrderWithItems(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDtoWithItems(order);
    }

    @Override
    @Cacheable(value = "orders-with-status", key = "#orderId", unless = "#result == null")
    public OrderDto getOrderWithStatus(Long orderId) {
        Order order = orderRepository.findByIdWithStatus(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDtoWithStatus(order);
    }

    @Override
    @Cacheable(value = "orders-by-correlation", key = "#correlationId", unless = "#result == null")
    public OrderDto getOrderByCorrelationId(UUID correlationId) {
        Order order = orderRepository.findByCorrelationIdAndIsDeletedFalse(correlationId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with correlation id: " + correlationId));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status"}, key = "#orderId")
    @Retryable(value = {OptimisticLockingException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public OrderDto updateOrder(Long orderId, OrderDto orderDto, Long currentUserId) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        validateOrderUpdate(existingOrder, orderDto, currentUserId);

        // Update allowed fields
        orderMapper.updateEntityFromDto(orderDto, existingOrder);

        // Recalculate total amount if order items changed
        if (orderDto.getOrderItems() != null && !orderDto.getOrderItems().isEmpty()) {
            BigDecimal newTotalAmount = calculateTotalAmount(existingOrder);
            existingOrder.setTotalAmount(newTotalAmount);
        }

        try {
            Order updatedOrder = orderRepository.save(existingOrder);
            log.info("Order updated successfully: orderId={}, userId={}", orderId, currentUserId);
            return orderMapper.toDto(updatedOrder);
        } catch (Exception e) {
            log.error("Failed to update order: {}", orderId, e);
            throw new OrderUpdateException("Failed to update order: " + e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status"}, key = "#orderId")
    public void cancelOrder(Long orderId, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        validateOrderCancellation(order, currentUserId);

        try {
            // Soft delete - the @SQLDelete annotation will handle this
            orderRepository.delete(order);
            log.info("Order cancelled successfully: orderId={}, userId={}", orderId, currentUserId);
        } catch (Exception e) {
            log.error("Failed to cancel order: {}", orderId, e);
            throw new OrderCancellationException("Failed to cancel order: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "user-orders", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        try {
            Page<Order> orders = orderRepository.findByUserIdAndIsDeletedFalseOrderByOrderDateDesc(userId, pageable);
            return orders.map(orderMapper::toDto);
        } catch (Exception e) {
            log.error("Failed to fetch orders for user: {}", userId, e);
            throw new OrderRetrievalException("Failed to retrieve orders for user: " + userId);
        }
    }

    @Override
    public List<OrderDto> getOrdersByUserIdAndDateRange(Long userId, OffsetDateTime startDate,
                                                        OffsetDateTime endDate, int limit) {
        validateDateRange(startDate, endDate);

        try {
            List<Order> orders = orderRepository.findByUserIdAndOrderDateBetween(userId, startDate, endDate);

            // Apply limit for performance
            List<Order> limitedOrders = orders.size() > limit ? orders.subList(0, limit) : orders;

            return limitedOrders.stream()
                    .map(orderMapper::toDto)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch orders for user {} in date range", userId, e);
            throw new OrderRetrievalException("Failed to retrieve orders for date range");
        }
    }

    @Override
    @Cacheable(value = "orders-by-status", key = "#statusId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<OrderDto> getOrdersByStatus(Long statusId, Pageable pageable) {
        try {
            Page<Order> orders = orderRepository.findByStatusStatusIdAndIsDeletedFalseOrderByOrderDateDesc(statusId, pageable);
            return orders.map(orderMapper::toDto);
        } catch (Exception e) {
            log.error("Failed to fetch orders by status: {}", statusId, e);
            throw new OrderRetrievalException("Failed to retrieve orders by status: " + statusId);
        }
    }

    @Override
    @Cacheable(value = "user-order-count", key = "#userId")
    public long countOrdersByUserId(Long userId) {
        try {
            return orderRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("Failed to count orders for user: {}", userId, e);
            throw new OrderRetrievalException("Failed to count orders for user: " + userId);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status", "orders-by-status"}, allEntries = true)
    @Retryable(value = {OptimisticLockingException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public OrderDto updateOrderStatus(Long orderId, Long statusId, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new OrderStatusNotFoundException("Order status not found with id: " + statusId));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        try {
            Order updatedOrder = orderRepository.save(order);
            log.info("Order status updated: orderId={}, oldStatus={}, newStatus={}, userId={}",
                    orderId, order.getStatus().getStatusName(), newStatus.getStatusName(), currentUserId);
            return orderMapper.toDto(updatedOrder);
        } catch (Exception e) {
            log.error("Failed to update order status: orderId={}, statusId={}", orderId, statusId, e);
            throw new OrderStatusUpdateException("Failed to update order status: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "order-summary", key = "'global'")
    public Map<String, Object> getOrderSummary() {
        try {
            // Implementation would return summary statistics
            // This is a placeholder for actual implementation
            return Map.of(
                    "totalOrders", orderRepository.count(),
                    "timestamp", OffsetDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to generate order summary", e);
            throw new OrderRetrievalException("Failed to generate order summary");
        }
    }

    @Override
    public boolean isOrderOwner(Long orderId, String username) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false;
            }
            // Implementation would check if the username corresponds to the order's user
            // This is a placeholder for actual authorization logic
            return true; // Placeholder
        } catch (Exception e) {
            log.error("Error checking order ownership: orderId={}, username={}", orderId, username, e);
            return false;
        }
    }

    // Private helper methods

    private void validateOrderCreation(OrderDto orderDto, Long currentUserId) {
        if (orderDto.getUserId() == null) {
            throw new InvalidOrderDataException("User ID is required");
        }

        if (orderDto.getOrderItems() == null || orderDto.getOrderItems().isEmpty()) {
            throw new InvalidOrderDataException("Order must contain at least one item");
        }

        // Additional business validations can be added here
    }

    private void validateOrderUpdate(Order existingOrder, OrderDto orderDto, Long currentUserId) {
        if (!canModifyOrder(existingOrder)) {
            throw new OrderModificationNotAllowedException("Order cannot be modified in current status");
        }

        // Additional business validations can be added here
    }

    private void validateOrderCancellation(Order order, Long currentUserId) {
        if (!canCancelOrder(order)) {
            throw new OrderCancellationNotAllowedException("Order cannot be cancelled in current status");
        }

        // Additional business validations can be added here
    }

    private boolean canModifyOrder(Order order) {
        // Business logic to determine if order can be modified
        String statusName = order.getStatus().getStatusName().toUpperCase();
        return "PENDING".equals(statusName) || "CONFIRMED".equals(statusName);
    }

    private boolean canCancelOrder(Order order) {
        // Business logic to determine if order can be cancelled
        String statusName = order.getStatus().getStatusName().toUpperCase();
        return !"DELIVERED".equals(statusName) && !"CANCELLED".equals(statusName);
    }

    private OrderStatus getDefaultOrderStatus() {
        return orderStatusRepository.findByStatusNameIgnoreCase("PENDING")
                .orElseThrow(() -> new OrderStatusNotFoundException("Default status 'PENDING' not found"));
    }

    private BigDecimal calculateTotalAmount(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return order.getOrderItems().stream()
                .map(item -> item.getFinalPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date must be before end date");
        }

        if (startDate.isBefore(OffsetDateTime.now().minusYears(1))) {
            throw new InvalidDateRangeException("Start date cannot be more than 1 year ago");
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Implement business rules for valid status transitions
        // This is a placeholder for actual business logic
        log.debug("Validating status transition from {} to {}",
                currentStatus.getStatusName(), newStatus.getStatusName());
    }
}