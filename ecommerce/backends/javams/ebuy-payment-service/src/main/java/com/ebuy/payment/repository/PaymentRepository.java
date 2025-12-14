package com.ebuy.payment.repository;


import com.ebuy.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIdAndIsDeletedFalse(Long paymentId);

    List<Payment> findAllByOrderIdAndIsDeletedFalse(Long orderId);

    Optional<Payment> findByCorrelationIdAndIsDeletedFalse(UUID correlationId);

    Optional<Payment> findByTransactionReferenceAndIsDeletedFalse(String transactionReference);

    Page<Payment> findAllByIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.orderId = :orderId " +
            "AND p.paymentStatus.paymentStatusId = :statusId " +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByOrderIdAndStatus(@Param("orderId") Long orderId,
                                         @Param("statusId") Short statusId);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.paymentStatus.paymentStatusId = :statusId " +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Payment> findByStatus(@Param("statusId") Short statusId, Pageable pageable);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.isDeleted = false " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") OffsetDateTime startDate,
                                           @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.paymentMethodType " +
            "LEFT JOIN FETCH p.paymentStatus " +
            "LEFT JOIN FETCH p.currencyCode " +
            "WHERE p.paymentId = :paymentId " +
            "AND p.isDeleted = false")
    Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

    @Query("SELECT COUNT(p) FROM Payment p " +
            "WHERE p.orderId = :orderId " +
            "AND p.paymentStatus.statusCode = :statusCode " +
            "AND p.isDeleted = false")
    long countByOrderIdAndStatusCode(@Param("orderId") Long orderId,
                                     @Param("statusCode") String statusCode);

    boolean existsByOrderIdAndIsDeletedFalse(Long orderId);
}