package com.ebuy.payment.service.impl;


import com.ebuy.payment.dto.mapper.PaymentMethodTypeMapper;
import com.ebuy.payment.dto.request.PaymentMethodTypeRequest;
import com.ebuy.payment.dto.response.PaymentMethodTypeResponse;
import com.ebuy.payment.entity.PaymentMethodType;
import com.ebuy.payment.exception.PaymentProcessingException;
import com.ebuy.payment.exception.ResourceNotFoundException;
import com.ebuy.payment.repository.PaymentMethodTypeRepository;
import com.ebuy.payment.service.PaymentMethodTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentMethodTypeServiceImpl implements PaymentMethodTypeService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodTypeServiceImpl.class);

    private final PaymentMethodTypeRepository paymentMethodTypeRepository;
    private final PaymentMethodTypeMapper paymentMethodTypeMapper;

    public PaymentMethodTypeServiceImpl(
            PaymentMethodTypeRepository paymentMethodTypeRepository,
            PaymentMethodTypeMapper paymentMethodTypeMapper) {
        this.paymentMethodTypeRepository = paymentMethodTypeRepository;
        this.paymentMethodTypeMapper = paymentMethodTypeMapper;
    }

    @Override
    public PaymentMethodTypeResponse createPaymentMethodType(PaymentMethodTypeRequest request) {
        logger.info("Creating payment method type: {}", request.getMethodName());

        // Check if method name already exists
        if (paymentMethodTypeRepository.existsByMethodNameAndIsDeletedFalse(request.getMethodName())) {
            throw new PaymentProcessingException(
                    "Payment method type already exists with name: " + request.getMethodName());
        }

        // Validate min/max amounts
        if (request.getMaxAmount() != null && request.getMinAmount() != null) {
            if (request.getMaxAmount().compareTo(request.getMinAmount()) < 0) {
                throw new PaymentProcessingException(
                        "Maximum amount cannot be less than minimum amount");
            }
        }

        PaymentMethodType paymentMethodType = paymentMethodTypeMapper.toEntity(request);
        paymentMethodType.setIsDeleted(false);

        PaymentMethodType savedPaymentMethodType = paymentMethodTypeRepository.save(paymentMethodType);

        logger.info("Payment method type created successfully with ID: {}",
                savedPaymentMethodType.getPaymentMethodTypeId());

        return paymentMethodTypeMapper.toResponse(savedPaymentMethodType);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodTypeResponse getPaymentMethodTypeById(Long id) {
        logger.debug("Fetching payment method type by ID: {}", id);

        PaymentMethodType paymentMethodType = paymentMethodTypeRepository
                .findByPaymentMethodTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethodType", "id", id));

        return paymentMethodTypeMapper.toResponse(paymentMethodType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodTypeResponse> getAllActivePaymentMethodTypes() {
        logger.debug("Fetching all active payment method types");

        List<PaymentMethodType> paymentMethodTypes =
                paymentMethodTypeRepository.findAllByIsDeletedFalseAndIsActiveTrueOrderByDisplayOrder();

        return paymentMethodTypeMapper.toResponseList(paymentMethodTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodTypeResponse> getPaymentMethodTypesByType(String methodType) {
        logger.debug("Fetching payment method types by type: {}", methodType);

        List<PaymentMethodType> paymentMethodTypes =
                paymentMethodTypeRepository.findAllByMethodTypeAndIsDeletedFalseAndIsActiveTrue(methodType);

        return paymentMethodTypeMapper.toResponseList(paymentMethodTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodTypeResponse> getAllPaymentMethodTypes(Pageable pageable) {
        logger.debug("Fetching all payment method types with pagination");

        Page<PaymentMethodType> paymentMethodTypesPage =
                paymentMethodTypeRepository.findAllByIsDeletedFalse(pageable);

        return paymentMethodTypesPage.map(paymentMethodTypeMapper::toResponse);
    }

    @Override
    public PaymentMethodTypeResponse updatePaymentMethodType(Long id, PaymentMethodTypeRequest request) {
        logger.info("Updating payment method type: {}", id);

        PaymentMethodType existingPaymentMethodType = paymentMethodTypeRepository
                .findByPaymentMethodTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethodType", "id", id));

        // Check if updating to a name that already exists (excluding current record)
        paymentMethodTypeRepository.findByMethodNameIgnoreCaseAndNotDeleted(request.getMethodName())
                .ifPresent(pmt -> {
                    if (!pmt.getPaymentMethodTypeId().equals(id)) {
                        throw new PaymentProcessingException(
                                "Payment method type already exists with name: " + request.getMethodName());
                    }
                });

        // Validate min/max amounts
        if (request.getMaxAmount() != null && request.getMinAmount() != null) {
            if (request.getMaxAmount().compareTo(request.getMinAmount()) < 0) {
                throw new PaymentProcessingException(
                        "Maximum amount cannot be less than minimum amount");
            }
        }

        paymentMethodTypeMapper.updateEntityFromRequest(request, existingPaymentMethodType);

        PaymentMethodType updatedPaymentMethodType =
                paymentMethodTypeRepository.save(existingPaymentMethodType);

        logger.info("Payment method type updated successfully: {}", id);

        return paymentMethodTypeMapper.toResponse(updatedPaymentMethodType);
    }

    @Override
    public void softDeletePaymentMethodType(Long id) {
        logger.info("Soft deleting payment method type: {}", id);

        PaymentMethodType paymentMethodType = paymentMethodTypeRepository
                .findByPaymentMethodTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethodType", "id", id));

        paymentMethodType.setIsDeleted(true);
        paymentMethodType.setDeletedAt(OffsetDateTime.now());
        paymentMethodTypeRepository.save(paymentMethodType);

        logger.info("Payment method type soft deleted successfully: {}", id);
    }

    @Override
    public void activatePaymentMethodType(Long id) {
        logger.info("Activating payment method type: {}", id);

        PaymentMethodType paymentMethodType = paymentMethodTypeRepository
                .findByPaymentMethodTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethodType", "id", id));

        paymentMethodType.setIsActive(true);
        paymentMethodTypeRepository.save(paymentMethodType);

        logger.info("Payment method type activated successfully: {}", id);
    }

    @Override
    public void deactivatePaymentMethodType(Long id) {
        logger.info("Deactivating payment method type: {}", id);

        PaymentMethodType paymentMethodType = paymentMethodTypeRepository
                .findByPaymentMethodTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethodType", "id", id));

        paymentMethodType.setIsActive(false);
        paymentMethodTypeRepository.save(paymentMethodType);

        logger.info("Payment method type deactivated successfully: {}", id);
    }
}
