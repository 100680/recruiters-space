package com.ebuy.order.controller;

import com.ebuy.order.dto.OrderItemDto;
import com.ebuy.order.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@Validated
@RequiredArgsConstructor
@Tag(name = "Order Item Management", description = "APIs for managing order items in eBuy platform")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "Add item to order", description = "Adds a new item to an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "422", description = "Business validation failed")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @CacheEvict(value = {"orders", "orders-with-items", "order-items"}, allEntries = true)
    public ResponseEntity<OrderItemDto> addOrderItem(
            @Valid @RequestBody OrderItemDto orderItemDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Adding item to order: {} for product: {}", orderItemDto.getOrderId(), orderItemDto.getProductId());
        OrderItemDto createdOrderItem = orderItemService.addOrderItem(orderItemDto, currentUserId);
        log.info("Order item added successfully with ID: {}", createdOrderItem.getOrderItemId());
        return new ResponseEntity<>(createdOrderItem, HttpStatus.CREATED);
    }

    @Operation(summary = "Get order item by ID", description = "Retrieves an order item by its unique identifier")
    @GetMapping("/{orderItemId}")
    @Cacheable(value = "order-items", key = "#orderItemId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN') or @orderItemService.isOrderItemAccessible(#orderItemId, authentication.name)")
    public ResponseEntity<OrderItemDto> getOrderItemById(
            @Parameter(description = "Order Item ID") @PathVariable @NotNull @Positive Long orderItemId) {

        log.debug("Fetching order item with ID: {}", orderItemId);
        OrderItemDto orderItem = orderItemService.getOrderItemById(orderItemId);
        return ResponseEntity.ok(orderItem);
    }

    @Operation(summary = "Update order item", description = "Updates an existing order item")
    @PutMapping("/{orderItemId}")
    @PreAuthorize("hasRole('ADMIN') or @orderItemService.isOrderItemAccessible(#orderItemId, authentication.name)")
    @CacheEvict(value = {"orders", "orders-with-items", "order-items"}, allEntries = true)
    public ResponseEntity<OrderItemDto> updateOrderItem(
            @Parameter(description = "Order Item ID") @PathVariable @NotNull @Positive Long orderItemId,
            @Valid @RequestBody OrderItemDto orderItemDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Updating order item with ID: {}", orderItemId);
        OrderItemDto updatedOrderItem = orderItemService.updateOrderItem(orderItemId, orderItemDto, currentUserId);
        log.info("Order item updated successfully: {}", orderItemId);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @Operation(summary = "Remove order item", description = "Removes an item from an order (soft delete)")
    @DeleteMapping("/{orderItemId}")
    @PreAuthorize("hasRole('ADMIN') or @orderItemService.isOrderItemAccessible(#orderItemId, authentication.name)")
    @CacheEvict(value = {"orders", "orders-with-items", "order-items"}, allEntries = true)
    public ResponseEntity<Void> removeOrderItem(
            @Parameter(description = "Order Item ID") @PathVariable @NotNull @Positive Long orderItemId,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Removing order item with ID: {}", orderItemId);
        orderItemService.removeOrderItem(orderItemId, currentUserId);
        log.info("Order item removed successfully: {}", orderItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get order items", description = "Retrieves all items for a specific order")
    @GetMapping("/order/{orderId}")
    @Cacheable(value = "order-items-by-order", key = "#orderId", unless = "#result.isEmpty()")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<List<OrderItemDto>> getOrderItemsByOrderId(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId) {

        log.debug("Fetching order items for order ID: {}", orderId);
        List<OrderItemDto> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @Operation(summary = "Get order item by order and product", description = "Retrieves a specific order item by order ID and product ID")
    @GetMapping("/order/{orderId}/product/{productId}")
    @Cacheable(value = "order-item-by-order-product", key = "#orderId + '-' + #productId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<OrderItemDto> getOrderItemByOrderIdAndProductId(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId,
            @Parameter(description = "Product ID") @PathVariable @NotNull @Positive Long productId) {

        log.debug("Fetching order item for order: {} and product: {}", orderId, productId);
        OrderItemDto orderItem = orderItemService.getOrderItemByOrderIdAndProductId(orderId, productId);
        return ResponseEntity.ok(orderItem);
    }

    @Operation(summary = "Check if product exists in order", description = "Checks if a specific product exists in an order")
    @GetMapping("/order/{orderId}/exists/product/{productId}")
    @Cacheable(value = "order-item-exists", key = "#orderId + '-' + #productId")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<Boolean> existsByOrderIdAndProductId(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId,
            @Parameter(description = "Product ID") @PathVariable @NotNull @Positive Long productId) {

        log.debug("Checking if product {} exists in order {}", productId, orderId);
        boolean exists = orderItemService.existsByOrderIdAndProductId(orderId, productId);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Count order items", description = "Returns the total count of items in an order")
    @GetMapping("/order/{orderId}/count")
    @Cacheable(value = "order-item-count", key = "#orderId")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<Long> countOrderItemsByOrderId(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId) {

        log.debug("Counting items for order: {}", orderId);
        long count = orderItemService.countOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific order item")
    @PatchMapping("/{orderItemId}/quantity/{quantity}")
    @PreAuthorize("hasRole('ADMIN') or @orderItemService.isOrderItemAccessible(#orderItemId, authentication.name)")
    @CacheEvict(value = {"orders", "orders-with-items", "order-items", "order-item-count"}, allEntries = true)
    public ResponseEntity<OrderItemDto> updateOrderItemQuantity(
            @Parameter(description = "Order Item ID") @PathVariable @NotNull @Positive Long orderItemId,
            @Parameter(description = "New quantity") @PathVariable @NotNull @Positive Integer quantity,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Updating quantity for order item: {} to: {}", orderItemId, quantity);
        OrderItemDto updatedOrderItem = orderItemService.updateOrderItemQuantity(orderItemId, quantity, currentUserId);
        log.info("Order item quantity updated successfully: {}", orderItemId);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @Operation(summary = "Bulk update order items", description = "Updates multiple order items in a single request")
    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @CacheEvict(value = {"orders", "orders-with-items", "order-items"}, allEntries = true)
    public ResponseEntity<List<OrderItemDto>> bulkUpdateOrderItems(
            @Valid @RequestBody List<OrderItemDto> orderItemDtos,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Bulk updating {} order items", orderItemDtos.size());
        List<OrderItemDto> updatedItems = orderItemService.bulkUpdateOrderItems(orderItemDtos, currentUserId);
        log.info("Bulk update completed for {} order items", updatedItems.size());
        return ResponseEntity.ok(updatedItems);
    }

    @Operation(summary = "Get items by product", description = "Retrieves all order items containing a specific product")
    @GetMapping("/product/{productId}")
    @Cacheable(value = "order-items-by-product", key = "#productId", unless = "#result.isEmpty()")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderItemDto>> getOrderItemsByProductId(
            @Parameter(description = "Product ID") @PathVariable @NotNull @Positive Long productId,
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "100") int limit) {

        // Limit for performance
        limit = Math.min(limit, 1000);

        log.debug("Fetching order items for product: {} with limit: {}", productId, limit);
        List<OrderItemDto> orderItems = orderItemService.getOrderItemsByProductId(productId, limit);
        return ResponseEntity.ok(orderItems);
    }
}