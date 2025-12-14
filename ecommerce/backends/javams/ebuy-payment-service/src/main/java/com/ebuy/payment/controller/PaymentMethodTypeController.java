package com.ebuy.payment.controller;


import com.ebuy.payment.dto.request.PaymentMethodTypeRequest;
import com.ebuy.payment.dto.response.ApiResponse;
import com.ebuy.payment.dto.response.PaymentMethodTypeResponse;
import com.ebuy.payment.service.PaymentMethodTypeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment-method-types")
@Tag(name = "Payment Method Type", description = "Payment method type management APIs")
public class PaymentMethodTypeController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodTypeController.class);

    private final PaymentMethodTypeService paymentMethodTypeService;

    public PaymentMethodTypeController(PaymentMethodTypeService paymentMethodTypeService) {
        this.paymentMethodTypeService = paymentMethodTypeService;
    }

    @PostMapping
    @Operation(summary = "Create payment method type",
            description = "Creates a new payment method type")
    public ResponseEntity<ApiResponse<PaymentMethodTypeResponse>> createPaymentMethodType(
            @Valid @RequestBody PaymentMethodTypeRequest request) {
        logger.info("Creating payment method type: {}", request.getMethodName());

        PaymentMethodTypeResponse paymentMethodType =
                paymentMethodTypeService.createPaymentMethodType(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment method type created successfully", paymentMethodType));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment method type by ID",
            description = "Retrieves a payment method type by its ID")
    public ResponseEntity<ApiResponse<PaymentMethodTypeResponse>> getPaymentMethodTypeById(
            @Parameter(description = "Payment method type ID") @PathVariable Long id) {
        logger.debug("Fetching payment method type: {}", id);

        PaymentMethodTypeResponse paymentMethodType =
                paymentMethodTypeService.getPaymentMethodTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(paymentMethodType));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active payment method types",
            description = "Retrieves all active payment method types")
    public ResponseEntity<ApiResponse<List<PaymentMethodTypeResponse>>> getAllActivePaymentMethodTypes() {
        logger.debug("Fetching all active payment method types");

        List<PaymentMethodTypeResponse> paymentMethodTypes =
                paymentMethodTypeService.getAllActivePaymentMethodTypes();
        return ResponseEntity.ok(ApiResponse.success(paymentMethodTypes));
    }

    @GetMapping("/type/{methodType}")
    @Operation(summary = "Get payment method types by type",
            description = "Retrieves all payment method types of a specific type")
    public ResponseEntity<ApiResponse<List<PaymentMethodTypeResponse>>> getPaymentMethodTypesByType(
            @Parameter(description = "Method type (e.g., CARD, WALLET)") @PathVariable String methodType) {
        logger.debug("Fetching payment method types by type: {}", methodType);

        List<PaymentMethodTypeResponse> paymentMethodTypes =
                paymentMethodTypeService.getPaymentMethodTypesByType(methodType);
        return ResponseEntity.ok(ApiResponse.success(paymentMethodTypes));
    }

    @GetMapping
    @Operation(summary = "Get all payment method types",
            description = "Retrieves all payment method types with pagination")
    public ResponseEntity<ApiResponse<Page<PaymentMethodTypeResponse>>> getAllPaymentMethodTypes(
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC)
            Pageable pageable) {
        logger.debug("Fetching all payment method types");

        Page<PaymentMethodTypeResponse> paymentMethodTypes =
                paymentMethodTypeService.getAllPaymentMethodTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(paymentMethodTypes));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment method type",
            description = "Updates an existing payment method type")
    public ResponseEntity<ApiResponse<PaymentMethodTypeResponse>> updatePaymentMethodType(
            @Parameter(description = "Payment method type ID") @PathVariable Long id,
            @Valid @RequestBody PaymentMethodTypeRequest request) {
        logger.info("Updating payment method type: {}", id);

        PaymentMethodTypeResponse paymentMethodType =
                paymentMethodTypeService.updatePaymentMethodType(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment method type updated successfully", paymentMethodType));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment method type",
            description = "Soft deletes a payment method type")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethodType(
            @Parameter(description = "Payment method type ID") @PathVariable Long id) {
        logger.info("Deleting payment method type: {}", id);

        paymentMethodTypeService.softDeletePaymentMethodType(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method type deleted successfully", null));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate payment method type",
            description = "Activates a payment method type")
    public ResponseEntity<ApiResponse<Void>> activatePaymentMethodType(
            @Parameter(description = "Payment method type ID") @PathVariable Long id) {
        logger.info("Activating payment method type: {}", id);

        paymentMethodTypeService.activatePaymentMethodType(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method type activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate payment method type",
            description = "Deactivates a payment method type")
    public ResponseEntity<ApiResponse<Void>> deactivatePaymentMethodType(
            @Parameter(description = "Payment method type ID") @PathVariable Long id) {
        logger.info("Deactivating payment method type: {}", id);

        paymentMethodTypeService.deactivatePaymentMethodType(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method type deactivated successfully", null));
    }
}
