package com.ebuy.payment.service.impl;


import com.ebuy.payment.dto.mapper.PaymentMapper;
import com.ebuy.payment.dto.request.CreatePaymentRequest;
import com.ebuy.payment.dto.request.UpdatePaymentStatusRequest;
import com.ebuy.payment.dto.response.PaymentDetailedResponse;
import com.ebuy.payment.dto.response.PaymentResponse;
import com.ebuy.payment.dto.response.PaymentStatusHistoryResponse;
import com.ebuy.payment.entity.*;
import com.ebuy.payment.exception.InvalidPaymentStatusException;
import com.ebuy.payment.exception.PaymentProcessingException;
import com.ebuy.payment.exception.ResourceNotFoundException;
import com.ebuy.payment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentMethodTypeRepository paymentMethodTypeRepository;
    private final CurrencyCodeRepository currencyCodeRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentStatusRepository paymentStatusRepository,
            PaymentMethodTypeRepository paymentMethodTypeRepository,
            CurrencyCodeRepository currencyCodeRepository,
            PaymentStatusHistoryRepository paymentStatusHistoryRepository,
            PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.paymentMethodTypeRepository = paymentMethodTypeRepository;
        this.currencyCodeRepository = currencyCodeRepository;
        this.paymentStatusHistoryRepository = paymentStatusHistoryRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        logger.info("Creating payment for order: {}", request.getOrderId());

        try {
            // Validate and fetch payment method type
            PaymentMethodType paymentMethodType = paymentMethodTypeRepository
                    .findByPaymentMethodTypeIdAndIsDeletedFalse(request.getPaymentMethodTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PaymentMethodType", "id", request.getPaymentMethodTypeId()));

            if (!paymentMethodType.getIsActive()) {
                throw new PaymentProcessingException(
                        "Payment method is not active: " + paymentMethodType.getMethodName());
            }

            // Validate amount against payment method limits
            validatePaymentAmount(request.getAmount(), paymentMethodType);

            // Fetch currency code
            CurrencyCode currencyCode = currencyCodeRepository
                    .findByCurrencyCodeIgnoreCaseAndActive(request.getCurrencyCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "CurrencyCode", "code", request.getCurrencyCode()));

            // Fetch initial payment status (PENDING)
            PaymentStatus initialStatus = paymentStatusRepository
                    .findByStatusCodeIgnoreCaseAndActive("PENDING")
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PaymentStatus", "code", "PENDING"));

            // Calculate processing fee if not provided
            BigDecimal processingFee = request.getProcessingFee();
            if (processingFee == null && paymentMethodType.getProcessingFeePercentage() != null) {
                processingFee = request.getAmount()
                        .multiply(paymentMethodType.getProcessingFeePercentage())
                        .divide(BigDecimal.valueOf(100))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            // Create payment entity
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .paymentMethodType(paymentMethodType)
                    .paymentStatus(initialStatus)
                    .currencyCode(currencyCode)
                    .amount(request.getAmount())
                    .processingFee(processingFee)
                    .transactionReference(request.getTransactionReference())
                    .serviceOrigin(request.getServiceOrigin())
                    .correlationId(UUID.randomUUID())
                    .isDeleted(false)
                    .build();

            // Save payment
            Payment savedPayment = paymentRepository.save(payment);

            // Create status history entry
            createStatusHistoryEntry(savedPayment, null, initialStatus,
                    "Payment created", "SYSTEM");

            logger.info("Payment created successfully with ID: {} for order: {}",
                    savedPayment.getPaymentId(), request.getOrderId());

            return paymentMapper.toPaymentResponse(savedPayment);

        } catch (ResourceNotFoundException | PaymentProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error creating payment for order: {}", request.getOrderId(), ex);
            throw new PaymentProcessingException(
                    "Failed to create payment: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailedResponse getPaymentById(Long paymentId) {
        logger.debug("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "id", paymentId));

        return paymentMapper.toPaymentDetailedResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailedResponse getPaymentByCorrelationId(UUID correlationId) {
        logger.debug("Fetching payment by correlation ID: {}", correlationId);

        Payment payment = paymentRepository.findByCorrelationIdAndIsDeletedFalse(correlationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "correlationId", correlationId));

        return paymentMapper.toPaymentDetailedResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDetailedResponse> getPaymentsByOrderId(Long orderId) {
        logger.debug("Fetching payments for order: {}", orderId);

        List<Payment> payments = paymentRepository.findAllByOrderIdAndIsDeletedFalse(orderId);
        return paymentMapper.toPaymentDetailedResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDetailedResponse> getAllPayments(Pageable pageable) {
        logger.debug("Fetching all payments with pagination");

        Page<Payment> paymentsPage = paymentRepository.findAllByIsDeletedFalse(pageable);
        return paymentsPage.map(paymentMapper::toPaymentDetailedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDetailedResponse> getPaymentsByStatus(String statusCode, Pageable pageable) {
        logger.debug("Fetching payments by status: {}", statusCode);

        PaymentStatus status = paymentStatusRepository.findByStatusCodeIgnoreCaseAndActive(statusCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentStatus", "code", statusCode));

        Page<Payment> paymentsPage = paymentRepository.findByStatus(
                status.getPaymentStatusId(), pageable);

        return paymentsPage.map(paymentMapper::toPaymentDetailedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDetailedResponse> getPaymentsByDateRange(
            OffsetDateTime startDate, OffsetDateTime endDate) {
        logger.debug("Fetching payments between {} and {}", startDate, endDate);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        return paymentMapper.toPaymentDetailedResponseList(payments);
    }

    @Override
    public PaymentDetailedResponse updatePaymentStatus(
            Long paymentId, UpdatePaymentStatusRequest request) {
        logger.info("Updating payment status for payment ID: {} to status: {}",
                paymentId, request.getStatusCode());

        try {
            // Fetch payment
            Payment payment = paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment", "id", paymentId));

            // Fetch new status
            PaymentStatus newStatus = paymentStatusRepository
                    .findByStatusCodeIgnoreCaseAndActive(request.getStatusCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PaymentStatus", "code", request.getStatusCode()));

            // Validate status transition
            PaymentStatus currentStatus = payment.getPaymentStatus();
            validateStatusTransition(currentStatus, newStatus);

            // Update payment
            PaymentStatus previousStatus = payment.getPaymentStatus();
            payment.setPaymentStatus(newStatus);

            // Update payment date when captured or completed
            if ("CAPTURED".equalsIgnoreCase(newStatus.getStatusCode()) && payment.getPaymentDate() == null) {
                payment.setPaymentDate(OffsetDateTime.now());
            }

            // Update failure reason if provided
            if (request.getFailureReason() != null) {
                payment.setFailureReason(request.getFailureReason());
            }

            // Update gateway response if provided
            if (request.getGatewayResponse() != null) {
                payment.setGatewayResponse(request.getGatewayResponse());
            }

            // Save payment
            Payment updatedPayment = paymentRepository.save(payment);

            // Create status history entry
            createStatusHistoryEntry(
                    updatedPayment,
                    previousStatus,
                    newStatus,
                    request.getReason(),
                    request.getChangedBy() != null ? request.getChangedBy() : "SYSTEM"
            );

            logger.info("Payment status updated successfully for payment ID: {}", paymentId);

            return paymentMapper.toPaymentDetailedResponse(updatedPayment);

        } catch (ResourceNotFoundException | InvalidPaymentStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error updating payment status for payment ID: {}", paymentId, ex);
            throw new PaymentProcessingException(
                    "Failed to update payment status: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void softDeletePayment(Long paymentId) {
        logger.info("Soft deleting payment: {}", paymentId);

        Payment payment = paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "id", paymentId));

        payment.setIsDeleted(true);
        payment.setDeletedAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        logger.info("Payment soft deleted successfully: {}", paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentStatusHistoryResponse> getPaymentStatusHistory(Long paymentId) {
        logger.debug("Fetching status history for payment: {}", paymentId);

        // Verify payment exists
        if (!paymentRepository.existsByOrderIdAndIsDeletedFalse(paymentId)) {
            throw new ResourceNotFoundException("Payment", "id", paymentId);
        }

        List<PaymentStatusHistory> history =
                paymentStatusHistoryRepository.findHistoryByPaymentId(paymentId);

        return paymentMapper.toPaymentStatusHistoryResponseList(history);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPaymentForOrder(Long orderId) {
        return paymentRepository.existsByOrderIdAndIsDeletedFalse(orderId);
    }

    // Private helper methods

    private void validatePaymentAmount(BigDecimal amount, PaymentMethodType paymentMethodType) {
        if (paymentMethodType.getMinAmount() != null &&
                amount.compareTo(paymentMethodType.getMinAmount()) < 0) {
            throw new PaymentProcessingException(
                    String.format("Amount %.2f is below minimum %.2f for payment method %s",
                            amount, paymentMethodType.getMinAmount(), paymentMethodType.getMethodName()));
        }

        if (paymentMethodType.getMaxAmount() != null &&
                amount.compareTo(paymentMethodType.getMaxAmount()) > 0) {
            throw new PaymentProcessingException(
                    String.format("Amount %.2f exceeds maximum %.2f for payment method %s",
                            amount, paymentMethodType.getMaxAmount(), paymentMethodType.getMethodName()));
        }
    }

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        // If current status is terminal, don't allow transitions
        if (currentStatus.getIsTerminal()) {
            throw new InvalidPaymentStatusException(
                    String.format("Cannot change status from terminal state: %s",
                            currentStatus.getStatusCode()));
        }

        // Add specific business rules for status transitions
        String currentCode = currentStatus.getStatusCode();
        String newCode = newStatus.getStatusCode();

        // Example: CAPTURED cannot go to AUTHORIZED
        if ("CAPTURED".equalsIgnoreCase(currentCode) && "AUTHORIZED".equalsIgnoreCase(newCode)) {
            throw new InvalidPaymentStatusException(currentCode, newCode);
        }

        // Add more validation rules as needed
    }

    private void createStatusHistoryEntry(
            Payment payment,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String reason,
            String changedBy) {

        PaymentStatusHistory history = PaymentStatusHistory.builder()
                .payment(payment)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedAt(OffsetDateTime.now())
                .changedBy(changedBy)
                .reason(reason)
                .correlationId(payment.getCorrelationId())
                .build();

        paymentStatusHistoryRepository.save(history);
    }
}