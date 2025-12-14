package com.ebuy.order.service;

import com.ebuy.order.dto.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for managing orders in the eBuy ecommerce platform.
 * Provides high-level business operations for order management.
 */
public interface OrderService {

    /**
     * Creates a new order for the specified user.
     *
     * @param orderDto the order data transfer object
     * @param currentUserId the ID of the current user performing the action
     * @return the created order DTO
     * @throws com.ebuy.order.exception.OrderCreationException if order creation fails
     * @throws com.ebuy.order.exception.InvalidOrderDataException if order data is invalid
     */
    OrderDto createOrder(OrderDto orderDto, Long currentUserId);

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param orderId the order ID
     * @return the order DTO
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     */
    OrderDto getOrderById(Long orderId);

    /**
     * Retrieves an order with all its items.
     *
     * @param orderId the order ID
     * @return the order DTO with items
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     */
    OrderDto getOrderWithItems(Long orderId);

    /**
     * Retrieves an order with status information.
     *
     * @param orderId the order ID
     * @return the order DTO with status
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     */
    OrderDto getOrderWithStatus(Long orderId);

    /**
     * Retrieves an order by its correlation ID.
     *
     * @param correlationId the correlation ID
     * @return the order DTO
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     */
    OrderDto getOrderByCorrelationId(UUID correlationId);

    /**
     * Updates an existing order.
     *
     * @param orderId the order ID
     * @param orderDto the updated order data
     * @param currentUserId the ID of the current user performing the action
     * @return the updated order DTO
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     * @throws com.ebuy.order.exception.OrderUpdateException if update fails
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if modification is not allowed
     */
    OrderDto updateOrder(Long orderId, OrderDto orderDto, Long currentUserId);

    /**
     * Cancels an order (soft delete).
     *
     * @param orderId the order ID
     * @param currentUserId the ID of the current user performing the action
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     * @throws com.ebuy.order.exception.OrderCancellationException if cancellation fails
     * @throws com.ebuy.order.exception.OrderCancellationNotAllowedException if cancellation is not allowed
     */
    void cancelOrder(Long orderId, Long currentUserId);

    /**
     * Retrieves paginated orders for a specific user.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of order DTOs
     * @throws com.ebuy.order.exception.OrderRetrievalException if retrieval fails
     */
    Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves orders for a user within a specific date range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @param limit maximum number of results
     * @return list of order DTOs
     * @throws com.ebuy.order.exception.OrderRetrievalException if retrieval fails
     * @throws com.ebuy.order.exception.InvalidDateRangeException if date range is invalid
     */
    List<OrderDto> getOrdersByUserIdAndDateRange(Long userId, OffsetDateTime startDate, OffsetDateTime endDate, int limit);

    /**
     * Retrieves paginated orders with a specific status.
     *
     * @param statusId the status ID
     * @param pageable pagination parameters
     * @return page of order DTOs
     * @throws com.ebuy.order.exception.OrderRetrievalException if retrieval fails
     */
    Page<OrderDto> getOrdersByStatus(Long statusId, Pageable pageable);

    /**
     * Counts the total number of orders for a specific user.
     *
     * @param userId the user ID
     * @return the count of orders
     * @throws com.ebuy.order.exception.OrderRetrievalException if count operation fails
     */
    long countOrdersByUserId(Long userId);

    /**
     * Updates the status of an existing order.
     *
     * @param orderId the order ID
     * @param statusId the new status ID
     * @param currentUserId the ID of the current user performing the action
     * @return the updated order DTO
     * @throws com.ebuy.order.exception.OrderNotFoundException if order is not found
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if status is not found
     * @throws com.ebuy.order.exception.OrderStatusUpdateException if status update fails
     */
    OrderDto updateOrderStatus(Long orderId, Long statusId, Long currentUserId);

    /**
     * Retrieves summary information about orders.
     *
     * @return map containing summary statistics
     * @throws com.ebuy.order.exception.OrderRetrievalException if summary generation fails
     */
    Map<String, Object> getOrderSummary();

    /**
     * Checks if the specified user owns the given order.
     * Used for authorization purposes.
     *
     * @param orderId the order ID
     * @param username the username to check
     * @return true if user owns the order, false otherwise
     */
    boolean isOrderOwner(Long orderId, String username);
}