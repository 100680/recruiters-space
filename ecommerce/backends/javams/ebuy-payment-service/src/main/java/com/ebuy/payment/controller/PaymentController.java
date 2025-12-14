package com.ebuy.payment.controller;

import com.ebuy.payment.dto.request.CreatePaymentRequest;
import com.ebuy.payment.dto.request.UpdatePaymentStatusRequest;
import com.ebuy.payment.dto.response.ApiResponse;
import com.ebuy.payment.dto.response.PaymentDetailedResponse;
import com.ebuy.payment.dto.response.PaymentResponse;
import com.ebuy.payment.dto.response.PaymentStatusHistoryResponse;
import com.ebuy.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Create a new payment", description = "Creates a new payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        logger.info("Creating payment for order: {}", request.getOrderId());

        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieves detailed payment information by payment ID")
    public ResponseEntity<ApiResponse<PaymentDetailedResponse>> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        logger.debug("Fetching payment: {}", paymentId);

        PaymentDetailedResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get payment by correlation ID",
            description = "Retrieves payment information by correlation ID")
    public ResponseEntity<ApiResponse<PaymentDetailedResponse>> getPaymentByCorrelationId(
            @Parameter(description = "Correlation ID") @PathVariable UUID correlationId) {
        logger.debug("Fetching payment by correlation ID: {}", correlationId);

        PaymentDetailedResponse payment = paymentService.getPaymentByCorrelationId(correlationId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order ID",
            description = "Retrieves all payments for a specific order")
    public ResponseEntity<ApiResponse<List<PaymentDetailedResponse>>> getPaymentsByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        logger.debug("Fetching payments for order: {}", orderId);

        List<PaymentDetailedResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping
    @Operation(summary = "Get all payments",
            description = "Retrieves all payments with pagination")
    public ResponseEntity<ApiResponse<Page<PaymentDetailedResponse>>> getAllPayments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        logger.debug("Fetching all payments");

        Page<PaymentDetailedResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/status/{statusCode}")
    @Operation(summary = "Get payments by status",
            description = "Retrieves all payments with a specific status")
    public ResponseEntity<ApiResponse<Page<PaymentDetailedResponse>>> getPaymentsByStatus(
            @Parameter(description = "Payment status code") @PathVariable String statusCode,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        logger.debug("Fetching payments with status: {}", statusCode);

        Page<PaymentDetailedResponse> payments = paymentService.getPaymentsByStatus(statusCode, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get payments by date range",
            description = "Retrieves all payments within a date range")
    public ResponseEntity<ApiResponse<List<PaymentDetailedResponse>>> getPaymentsByDateRange(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        logger.debug("Fetching payments between {} and {}", startDate, endDate);

        List<PaymentDetailedResponse> payments =
                paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PatchMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status",
            description = "Updates the status of a payment")
    public ResponseEntity<ApiResponse<PaymentDetailedResponse>> updatePaymentStatus(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        logger.info("Updating payment status for payment: {}", paymentId);

        PaymentDetailedResponse payment = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", payment));
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Delete payment",
            description = "Soft deletes a payment (marks as deleted)")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        logger.info("Deleting payment: {}", paymentId);

        paymentService.softDeletePayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
    }

    @GetMapping("/{paymentId}/history")
    @Operation(summary = "Get payment status history",
            description = "Retrieves the status change history for a payment")
    public ResponseEntity<ApiResponse<List<PaymentStatusHistoryResponse>>> getPaymentStatusHistory(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        logger.debug("Fetching status history for payment: {}", paymentId);

        List<PaymentStatusHistoryResponse> history =
                paymentService.getPaymentStatusHistory(paymentId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/order/{orderId}/exists")
    @Operation(summary = "Check if payment exists for order",
            description = "Checks if any payment exists for the given order")
    public ResponseEntity<ApiResponse<Boolean>> checkPaymentExistsForOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        logger.debug("Checking if payment exists for order: {}", orderId);

        boolean exists = paymentService.existsPaymentForOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}