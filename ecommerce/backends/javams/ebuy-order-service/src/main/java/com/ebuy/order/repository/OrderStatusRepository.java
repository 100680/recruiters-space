package com.ebuy.order.repository;

import com.ebuy.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    @Query("SELECT os FROM OrderStatus os WHERE LOWER(os.statusName) = LOWER(:statusName) AND os.isDeleted = false")
    Optional<OrderStatus> findByStatusNameIgnoreCase(@Param("statusName") String statusName);

    boolean existsByStatusNameIgnoreCaseAndIsDeletedFalse(String statusName);
}