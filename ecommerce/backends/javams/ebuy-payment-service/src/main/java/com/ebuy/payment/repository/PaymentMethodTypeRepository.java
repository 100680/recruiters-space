package com.ebuy.payment.repository;


import com.ebuy.payment.entity.PaymentMethodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodTypeRepository extends JpaRepository<PaymentMethodType, Long> {

    Optional<PaymentMethodType> findByPaymentMethodTypeIdAndIsDeletedFalse(Long id);

    List<PaymentMethodType> findAllByIsDeletedFalseAndIsActiveTrueOrderByDisplayOrder();

    List<PaymentMethodType> findAllByMethodTypeAndIsDeletedFalseAndIsActiveTrue(String methodType);

    Page<PaymentMethodType> findAllByIsDeletedFalse(Pageable pageable);

    @Query("SELECT pmt FROM PaymentMethodType pmt WHERE LOWER(pmt.methodName) = LOWER(?1) AND pmt.isDeleted = false")
    Optional<PaymentMethodType> findByMethodNameIgnoreCaseAndNotDeleted(String methodName);

    boolean existsByMethodNameAndIsDeletedFalse(String methodName);
}
