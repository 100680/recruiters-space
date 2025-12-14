package com.ebuy.order.controller;

import com.ebuy.order.dto.OrderDto;
import com.ebuy.order.service.OrderService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders in eBuy platform")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "422", description = "Business validation failed")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody OrderDto orderDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Creating order for user: {}", orderDto.getUserId());
        OrderDto createdOrder = orderService.createOrder(orderDto, currentUserId);
        log.info("Order created successfully with ID: {}", createdOrder.getOrderId());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves an order by its unique identifier")
    @GetMapping("/{orderId}")
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId) {

        log.debug("Fetching order with ID: {}", orderId);
        OrderDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get order with items", description = "Retrieves an order with all its items")
    @GetMapping("/{orderId}/with-items")
    @Cacheable(value = "orders-with-items", key = "#orderId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<OrderDto> getOrderWithItems(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId) {

        log.debug("Fetching order with items for ID: {}", orderId);
        OrderDto order = orderService.getOrderWithItems(orderId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get order with status", description = "Retrieves an order with status information")
    @GetMapping("/{orderId}/with-status")
    @Cacheable(value = "orders-with-status", key = "#orderId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<OrderDto> getOrderWithStatus(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId) {

        log.debug("Fetching order with status for ID: {}", orderId);
        OrderDto order = orderService.getOrderWithStatus(orderId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get order by correlation ID", description = "Retrieves an order by its correlation identifier")
    @GetMapping("/correlation/{correlationId}")
    @Cacheable(value = "orders-by-correlation", key = "#correlationId", unless = "#result == null")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> getOrderByCorrelationId(
            @Parameter(description = "Correlation ID") @PathVariable @NotNull UUID correlationId) {

        log.debug("Fetching order with correlation ID: {}", correlationId);
        OrderDto order = orderService.getOrderByCorrelationId(correlationId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Update order", description = "Updates an existing order")
    @PutMapping("/{orderId}")
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status"}, key = "#orderId")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<OrderDto> updateOrder(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId,
            @Valid @RequestBody OrderDto orderDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Updating order with ID: {}", orderId);
        OrderDto updatedOrder = orderService.updateOrder(orderId, orderDto, currentUserId);
        log.info("Order updated successfully: {}", orderId);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Cancel order", description = "Cancels an existing order (soft delete)")
    @DeleteMapping("/{orderId}")
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status"}, key = "#orderId")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Cancelling order with ID: {}", orderId);
        orderService.cancelOrder(orderId, currentUserId);
        log.info("Order cancelled successfully: {}", orderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user orders", description = "Retrieves paginated orders for a specific user")
    @GetMapping("/user/{userId}")
    @Cacheable(value = "user-orders", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<Page<OrderDto>> getOrdersByUserId(
            @Parameter(description = "User ID") @PathVariable @NotNull @Positive Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "orderDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        // Limit page size for performance
        size = Math.min(size, 100);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        log.debug("Fetching orders for user: {} with pagination: {}", userId, pageable);
        Page<OrderDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by date range", description = "Retrieves orders within a specific date range")
    @GetMapping("/user/{userId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<List<OrderDto>> getOrdersByUserIdAndDateRange(
            @Parameter(description = "User ID") @PathVariable @NotNull @Positive Long userId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "1000") int limit) {

        // Limit for performance
        limit = Math.min(limit, 1000);

        log.debug("Fetching orders for user: {} between {} and {}", userId, startDate, endDate);
        List<OrderDto> orders = orderService.getOrdersByUserIdAndDateRange(userId, startDate, endDate, limit);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by status", description = "Retrieves paginated orders with specific status")
    @GetMapping("/status/{statusId}")
    @Cacheable(value = "orders-by-status", key = "#statusId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @Parameter(description = "Status ID") @PathVariable @NotNull @Positive Long statusId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

        log.debug("Fetching orders with status: {} with pagination: {}", statusId, pageable);
        Page<OrderDto> orders = orderService.getOrdersByStatus(statusId, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Count user orders", description = "Returns the total count of orders for a user")
    @GetMapping("/user/{userId}/count")
    @Cacheable(value = "user-order-count", key = "#userId")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<Long> countOrdersByUserId(
            @Parameter(description = "User ID") @PathVariable @NotNull @Positive Long userId) {

        log.debug("Counting orders for user: {}", userId);
        long count = orderService.countOrdersByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @PatchMapping("/{orderId}/status/{statusId}")
    @CacheEvict(value = {"orders", "orders-with-items", "orders-with-status", "orders-by-status"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable @NotNull @Positive Long orderId,
            @Parameter(description = "New Status ID") @PathVariable @NotNull @Positive Long statusId,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Updating status for order: {} to status: {}", orderId, statusId);
        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, statusId, currentUserId);
        log.info("Order status updated successfully: {}", orderId);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Get order summary", description = "Returns summarized information about orders")
    @GetMapping("/summary")
    @Cacheable(value = "order-summary", key = "'global'")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOrderSummary() {
        log.debug("Fetching order summary");
        Object summary = orderService.getOrderSummary();
        return ResponseEntity.ok(summary);
    }
}