package com.ebuy.payment.repository;


import com.ebuy.payment.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    List<PaymentStatusHistory> findByPayment_PaymentIdOrderByChangedAtDesc(Long paymentId);

    @Query("SELECT psh FROM PaymentStatusHistory psh " +
            "WHERE psh.payment.paymentId = :paymentId " +
            "ORDER BY psh.changedAt DESC")
    List<PaymentStatusHistory> findHistoryByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT psh FROM PaymentStatusHistory psh " +
            "WHERE psh.newStatus.paymentStatusId = :statusId " +
            "ORDER BY psh.changedAt DESC")
    List<PaymentStatusHistory> findByNewStatusId(@Param("statusId") Short statusId);
}
