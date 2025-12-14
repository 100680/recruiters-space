package com.ebuy.payment.dto.mapper;

import com.ebuy.payment.dto.response.PaymentDetailedResponse;
import com.ebuy.payment.dto.response.PaymentResponse;
import com.ebuy.payment.dto.response.PaymentStatusHistoryResponse;
import com.ebuy.payment.entity.Payment;
import com.ebuy.payment.entity.PaymentStatusHistory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "paymentMethodType.paymentMethodTypeId", target = "paymentMethodTypeId")
    @Mapping(source = "paymentStatus.paymentStatusId", target = "paymentStatusId")
    @Mapping(source = "currencyCode.currencyCodeId", target = "currencyCodeId")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(source = "paymentMethodType.paymentMethodTypeId", target = "paymentMethodTypeId")
    @Mapping(source = "paymentMethodType.methodType", target = "methodType")
    @Mapping(source = "paymentMethodType.methodName", target = "paymentMethodName")
    @Mapping(source = "paymentStatus.paymentStatusId", target = "paymentStatusId")
    @Mapping(source = "paymentStatus.statusCode", target = "statusCode")
    @Mapping(source = "paymentStatus.statusName", target = "paymentStatusName")
    @Mapping(source = "paymentStatus.isTerminal", target = "isTerminalStatus")
    @Mapping(source = "currencyCode.currencyCodeId", target = "currencyCodeId")
    @Mapping(source = "currencyCode.currencyCode", target = "currencyCode")
    @Mapping(source = "currencyCode.currencyName", target = "currencyName")
    @Mapping(source = "currencyCode.currencySymbol", target = "currencySymbol")
    PaymentDetailedResponse toPaymentDetailedResponse(Payment payment);

    List<PaymentResponse> toPaymentResponseList(List<Payment> payments);

    List<PaymentDetailedResponse> toPaymentDetailedResponseList(List<Payment> payments);

    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "previousStatus.statusCode", target = "previousStatusCode")
    @Mapping(source = "previousStatus.statusName", target = "previousStatusName")
    @Mapping(source = "newStatus.statusCode", target = "newStatusCode")
    @Mapping(source = "newStatus.statusName", target = "newStatusName")
    PaymentStatusHistoryResponse toPaymentStatusHistoryResponse(PaymentStatusHistory history);

    List<PaymentStatusHistoryResponse> toPaymentStatusHistoryResponseList(List<PaymentStatusHistory> histories);
}