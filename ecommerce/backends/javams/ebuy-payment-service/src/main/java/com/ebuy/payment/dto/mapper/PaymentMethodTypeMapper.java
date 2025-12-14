package com.ebuy.payment.dto.mapper;

import com.ebuy.payment.dto.request.PaymentMethodTypeRequest;
import com.ebuy.payment.dto.response.PaymentMethodTypeResponse;
import com.ebuy.payment.entity.PaymentMethodType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodTypeMapper {

    PaymentMethodTypeResponse toResponse(PaymentMethodType paymentMethodType);

    List<PaymentMethodTypeResponse> toResponseList(List<PaymentMethodType> paymentMethodTypes);

    @Mapping(target = "paymentMethodTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    PaymentMethodType toEntity(PaymentMethodTypeRequest request);

    @Mapping(target = "paymentMethodTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PaymentMethodTypeRequest request, @MappingTarget PaymentMethodType paymentMethodType);
}
