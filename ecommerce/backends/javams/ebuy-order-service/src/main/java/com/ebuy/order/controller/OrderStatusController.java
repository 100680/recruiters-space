package com.ebuy.order.controller;

import com.ebuy.order.dto.OrderStatusDto;
import com.ebuy.order.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/order-status")
@Validated
@RequiredArgsConstructor
@Tag(name = "Order Status Management", description = "APIs for managing order statuses in eBuy platform")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @Operation(summary = "Create order status", description = "Creates a new order status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order status created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Status already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "order-statuses", allEntries = true)
    public ResponseEntity<OrderStatusDto> createOrderStatus(
            @Valid @RequestBody OrderStatusDto orderStatusDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Creating order status: {}", orderStatusDto.getStatusName());
        OrderStatusDto createdStatus = orderStatusService.createOrderStatus(orderStatusDto, currentUserId);
        log.info("Order status created successfully with ID: {}", createdStatus.getStatusId());
        return new ResponseEntity<>(createdStatus, HttpStatus.CREATED);
    }

    @Operation(summary = "Get order status by ID", description = "Retrieves an order status by its unique identifier")
    @GetMapping("/{statusId}")
    @Cacheable(value = "order-statuses", key = "#statusId", unless = "#result == null")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderStatusDto> getOrderStatusById(
            @Parameter(description = "Status ID") @PathVariable @NotNull @Positive Long statusId) {

        log.debug("Fetching order status with ID: {}", statusId);
        OrderStatusDto orderStatus = orderStatusService.getOrderStatusById(statusId);
        return ResponseEntity.ok(orderStatus);
    }

    @Operation(summary = "Get order status by name", description = "Retrieves an order status by its name")
    @GetMapping("/name/{statusName}")
    @Cacheable(value = "order-statuses-by-name", key = "#statusName.toLowerCase()", unless = "#result == null")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderStatusDto> getOrderStatusByName(
            @Parameter(description = "Status name") @PathVariable @NotBlank String statusName) {

        log.debug("Fetching order status with name: {}", statusName);
        OrderStatusDto orderStatus = orderStatusService.getOrderStatusByName(statusName);
        return ResponseEntity.ok(orderStatus);
    }

    @Operation(summary = "Get all order statuses", description = "Retrieves all available order statuses")
    @GetMapping
    @Cacheable(value = "all-order-statuses", unless = "#result.isEmpty()")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderStatusDto>> getAllOrderStatuses() {
        log.debug("Fetching all order statuses");
        List<OrderStatusDto> orderStatuses = orderStatusService.getAllOrderStatuses();
        return ResponseEntity.ok(orderStatuses);
    }

    @Operation(summary = "Update order status", description = "Updates an existing order status")
    @PutMapping("/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"order-statuses", "order-statuses-by-name", "all-order-statuses"}, allEntries = true)
    public ResponseEntity<OrderStatusDto> updateOrderStatus(
            @Parameter(description = "Status ID") @PathVariable @NotNull @Positive Long statusId,
            @Valid @RequestBody OrderStatusDto orderStatusDto,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Updating order status with ID: {}", statusId);
        OrderStatusDto updatedStatus = orderStatusService.updateOrderStatus(statusId, orderStatusDto, currentUserId);
        log.info("Order status updated successfully: {}", statusId);
        return ResponseEntity.ok(updatedStatus);
    }

    @Operation(summary = "Delete order status", description = "Deletes an order status (soft delete)")
    @DeleteMapping("/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"order-statuses", "order-statuses-by-name", "all-order-statuses"}, allEntries = true)
    public ResponseEntity<Void> deleteOrderStatus(
            @Parameter(description = "Status ID") @PathVariable @NotNull @Positive Long statusId,
            @RequestHeader(value = "X-User-ID", required = false) Long currentUserId) {

        log.info("Deleting order status with ID: {}", statusId);
        orderStatusService.deleteOrderStatus(statusId, currentUserId);
        log.info("Order status deleted successfully: {}", statusId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if status exists", description = "Checks if an order status exists by name")
    @GetMapping("/exists/name/{statusName}")
    @Cacheable(value = "status-exists", key = "#statusName.toLowerCase()")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByStatusName(
            @Parameter(description = "Status name") @PathVariable @NotBlank String statusName) {

        log.debug("Checking existence of status: {}", statusName);
        boolean exists = orderStatusService.existsByStatusName(statusName);
        return ResponseEntity.ok(exists);
    }
}