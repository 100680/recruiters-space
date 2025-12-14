package com.ebuy.payment.repository;

import com.ebuy.payment.entity.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyCodeRepository extends JpaRepository<CurrencyCode, Short> {

    Optional<CurrencyCode> findByCurrencyCodeAndIsActiveTrue(String currencyCode);

    List<CurrencyCode> findAllByIsActiveTrueOrderByDisplayOrder();

    @Query("SELECT c FROM CurrencyCode c WHERE UPPER(c.currencyCode) = UPPER(?1) AND c.isActive = true")
    Optional<CurrencyCode> findByCurrencyCodeIgnoreCaseAndActive(String currencyCode);

    boolean existsByCurrencyCodeAndIsActiveTrue(String currencyCode);
}