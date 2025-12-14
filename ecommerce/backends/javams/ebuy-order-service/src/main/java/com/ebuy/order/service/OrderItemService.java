package com.ebuy.order.service;

import com.ebuy.order.dto.OrderItemDto;

import java.util.List;

/**
 * Service interface for managing order items in the eBuy ecommerce platform.
 * Provides high-level business operations for order item management.
 */
public interface OrderItemService {

    /**
     * Adds a new item to an existing order.
     *
     * @param orderItemDto the order item data transfer object
     * @param currentUserId the ID of the current user performing the action
     * @return the created order item DTO
     * @throws com.ebuy.order.exception.OrderItemCreationException if item creation fails
     * @throws com.ebuy.order.exception.InvalidOrderItemDataException if item data is invalid
     * @throws com.ebuy.order.exception.OrderNotFoundException if the associated order is not found
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if order cannot be modified
     */
    OrderItemDto addOrderItem(OrderItemDto orderItemDto, Long currentUserId);

    /**
     * Retrieves an order item by its unique identifier.
     *
     * @param orderItemId the order item ID
     * @return the order item DTO
     * @throws com.ebuy.order.exception.OrderItemNotFoundException if order item is not found
     */
    OrderItemDto getOrderItemById(Long orderItemId);

    /**
     * Updates an existing order item.
     *
     * @param orderItemId the order item ID
     * @param orderItemDto the updated order item data
     * @param currentUserId the ID of the current user performing the action
     * @return the updated order item DTO
     * @throws com.ebuy.order.exception.OrderItemNotFoundException if order item is not found
     * @throws com.ebuy.order.exception.OrderItemUpdateException if update fails
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if modification is not allowed
     */
    OrderItemDto updateOrderItem(Long orderItemId, OrderItemDto orderItemDto, Long currentUserId);

    /**
     * Removes an order item (soft delete).
     *
     * @param orderItemId the order item ID
     * @param currentUserId the ID of the current user performing the action
     * @throws com.ebuy.order.exception.OrderItemNotFoundException if order item is not found
     * @throws com.ebuy.order.exception.OrderItemDeletionException if deletion fails
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if deletion is not allowed
     */
    void removeOrderItem(Long orderItemId, Long currentUserId);

    /**
     * Retrieves all order items for a specific order.
     *
     * @param orderId the order ID
     * @return list of order item DTOs
     * @throws com.ebuy.order.exception.OrderItemRetrievalException if retrieval fails
     */
    List<OrderItemDto> getOrderItemsByOrderId(Long orderId);

    /**
     * Retrieves a specific order item by order ID and product ID.
     *
     * @param orderId the order ID
     * @param productId the product ID
     * @return the order item DTO
     * @throws com.ebuy.order.exception.OrderItemNotFoundException if order item is not found
     */
    OrderItemDto getOrderItemByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * Checks if an order item exists for the given order and product.
     *
     * @param orderId the order ID
     * @param productId the product ID
     * @return true if order item exists, false otherwise
     */
    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * Counts the total number of items in a specific order.
     *
     * @param orderId the order ID
     * @return the count of order items
     * @throws com.ebuy.order.exception.OrderItemRetrievalException if count operation fails
     */
    long countOrderItemsByOrderId(Long orderId);

    /**
     * Updates the quantity of a specific order item.
     *
     * @param orderItemId the order item ID
     * @param quantity the new quantity
     * @param currentUserId the ID of the current user performing the action
     * @return the updated order item DTO
     * @throws com.ebuy.order.exception.OrderItemNotFoundException if order item is not found
     * @throws com.ebuy.order.exception.OrderItemUpdateException if update fails
     * @throws com.ebuy.order.exception.InvalidOrderItemDataException if quantity is invalid
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if modification is not allowed
     */
    OrderItemDto updateOrderItemQuantity(Long orderItemId, Integer quantity, Long currentUserId);

    /**
     * Updates multiple order items in a single transaction.
     * Limited to 100 items per request for performance reasons.
     *
     * @param orderItemDtos list of order item DTOs to update
     * @param currentUserId the ID of the current user performing the action
     * @return list of updated order item DTOs
     * @throws com.ebuy.order.exception.OrderItemUpdateException if bulk update fails
     * @throws com.ebuy.order.exception.InvalidOrderItemDataException if data is invalid
     * @throws com.ebuy.order.exception.OrderModificationNotAllowedException if modification is not allowed
     */
    List<OrderItemDto> bulkUpdateOrderItems(List<OrderItemDto> orderItemDtos, Long currentUserId);

    /**
     * Retrieves order items containing a specific product.
     * Used for analytics and product management.
     *
     * @param productId the product ID
     * @param limit maximum number of results (max 1000)
     * @return list of order item DTOs
     * @throws com.ebuy.order.exception.OrderItemRetrievalException if retrieval fails
     */
    List<OrderItemDto> getOrderItemsByProductId(Long productId, int limit);

    /**
     * Checks if the specified user can access the given order item.
     * Used for authorization purposes.
     *
     * @param orderItemId the order item ID
     * @param username the username to check
     * @return true if user can access the order item, false otherwise
     */
    boolean isOrderItemAccessible(Long orderItemId, String username);
}