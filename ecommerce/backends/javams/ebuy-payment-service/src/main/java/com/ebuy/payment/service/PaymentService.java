package com.ebuy.payment.service;

import com.ebuy.payment.dto.request.CreatePaymentRequest;
import com.ebuy.payment.dto.request.UpdatePaymentStatusRequest;
import com.ebuy.payment.dto.response.PaymentDetailedResponse;
import com.ebuy.payment.dto.response.PaymentResponse;
import com.ebuy.payment.dto.response.PaymentStatusHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentDetailedResponse getPaymentById(Long paymentId);

    PaymentDetailedResponse getPaymentByCorrelationId(UUID correlationId);

    List<PaymentDetailedResponse> getPaymentsByOrderId(Long orderId);

    Page<PaymentDetailedResponse> getAllPayments(Pageable pageable);

    Page<PaymentDetailedResponse> getPaymentsByStatus(String statusCode, Pageable pageable);

    List<PaymentDetailedResponse> getPaymentsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);

    PaymentDetailedResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);

    void softDeletePayment(Long paymentId);

    List<PaymentStatusHistoryResponse> getPaymentStatusHistory(Long paymentId);

    boolean existsPaymentForOrder(Long orderId);
}