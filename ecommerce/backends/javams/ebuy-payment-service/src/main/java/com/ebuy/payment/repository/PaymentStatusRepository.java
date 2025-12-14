package com.ebuy.payment.repository;


import com.ebuy.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Short> {

    Optional<PaymentStatus> findByStatusCodeAndIsActiveTrue(String statusCode);

    List<PaymentStatus> findAllByIsActiveTrueOrderByDisplayOrder();

    List<PaymentStatus> findAllByIsTerminalTrueAndIsActiveTrue();

    @Query("SELECT ps FROM PaymentStatus ps WHERE UPPER(ps.statusCode) = UPPER(?1) AND ps.isActive = true")
    Optional<PaymentStatus> findByStatusCodeIgnoreCaseAndActive(String statusCode);

    boolean existsByStatusCodeAndIsActiveTrue(String statusCode);
}