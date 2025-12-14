package com.ebuy.order.service;

import com.ebuy.order.dto.OrderStatusDto;

import java.util.List;

/**
 * Service interface for managing order statuses in the eBuy ecommerce platform.
 * Provides high-level business operations for order status management.
 */
public interface OrderStatusService {

    /**
     * Creates a new order status.
     *
     * @param orderStatusDto the order status data transfer object
     * @param currentUserId the ID of the current user performing the action
     * @return the created order status DTO
     * @throws com.ebuy.order.exception.OrderStatusCreationException if status creation fails
     * @throws com.ebuy.order.exception.InvalidOrderStatusDataException if status data is invalid
     * @throws com.ebuy.order.exception.OrderStatusAlreadyExistsException if status already exists
     */
    OrderStatusDto createOrderStatus(OrderStatusDto orderStatusDto, Long currentUserId);

    /**
     * Retrieves an order status by its unique identifier.
     *
     * @param statusId the status ID
     * @return the order status DTO
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if status is not found
     */
    OrderStatusDto getOrderStatusById(Long statusId);

    /**
     * Retrieves an order status by its name (case-insensitive).
     *
     * @param statusName the status name
     * @return the order status DTO
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if status is not found
     */
    OrderStatusDto getOrderStatusByName(String statusName);

    /**
     * Retrieves all available order statuses.
     *
     * @return list of all order status DTOs
     * @throws com.ebuy.order.exception.OrderStatusRetrievalException if retrieval fails
     */
    List<OrderStatusDto> getAllOrderStatuses();

    /**
     * Updates an existing order status.
     *
     * @param statusId the status ID
     * @param orderStatusDto the updated status data
     * @param currentUserId the ID of the current user performing the action
     * @return the updated order status DTO
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if status is not found
     * @throws com.ebuy.order.exception.OrderStatusUpdateException if update fails
     * @throws com.ebuy.order.exception.InvalidOrderStatusDataException if status data is invalid
     * @throws com.ebuy.order.exception.OrderStatusAlreadyExistsException if new name already exists
     */
    OrderStatusDto updateOrderStatus(Long statusId, OrderStatusDto orderStatusDto, Long currentUserId);

    /**
     * Deletes an order status (soft delete).
     * System statuses cannot be deleted.
     *
     * @param statusId the status ID
     * @param currentUserId the ID of the current user performing the action
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if status is not found
     * @throws com.ebuy.order.exception.OrderStatusDeletionException if deletion fails or not allowed
     */
    void deleteOrderStatus(Long statusId, Long currentUserId);

    /**
     * Checks if an order status exists with the given name.
     *
     * @param statusName the status name to check
     * @return true if status exists, false otherwise
     */
    boolean existsByStatusName(String statusName);

    /**
     * Retrieves all active order statuses.
     * Active statuses are those that can be used for new orders.
     *
     * @return list of active order status DTOs
     * @throws com.ebuy.order.exception.OrderStatusRetrievalException if retrieval fails
     */
    List<OrderStatusDto> getActiveOrderStatuses();

    /**
     * Retrieves the default order status for new orders.
     *
     * @return the default order status DTO
     * @throws com.ebuy.order.exception.OrderStatusNotFoundException if default status is not found
     * @throws com.ebuy.order.exception.OrderStatusRetrievalException if retrieval fails
     */
    OrderStatusDto getDefaultOrderStatus();

    /**
     * Validates if a status transition is allowed based on business rules.
     *
     * @param fromStatusId the current status ID
     * @param toStatusId the target status ID
     * @return true if transition is valid, false otherwise
     */
    boolean isValidStatusTransition(Long fromStatusId, Long toStatusId);
}