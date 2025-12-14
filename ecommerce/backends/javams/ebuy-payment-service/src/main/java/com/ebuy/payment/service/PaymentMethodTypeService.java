package com.ebuy.payment.service;


import com.ebuy.payment.dto.request.PaymentMethodTypeRequest;
import com.ebuy.payment.dto.response.PaymentMethodTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentMethodTypeService {

    PaymentMethodTypeResponse createPaymentMethodType(PaymentMethodTypeRequest request);

    PaymentMethodTypeResponse getPaymentMethodTypeById(Long id);

    List<PaymentMethodTypeResponse> getAllActivePaymentMethodTypes();

    List<PaymentMethodTypeResponse> getPaymentMethodTypesByType(String methodType);

    Page<PaymentMethodTypeResponse> getAllPaymentMethodTypes(Pageable pageable);

    PaymentMethodTypeResponse updatePaymentMethodType(Long id, PaymentMethodTypeRequest request);

    void softDeletePaymentMethodType(Long id);

    void activatePaymentMethodType(Long id);

    void deactivatePaymentMethodType(Long id);
}