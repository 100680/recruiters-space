package com.ebuy.order.repository;

import com.ebuy.order.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndIsDeletedFalseOrderByOrderDateDesc(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.isDeleted = false ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndOrderDateBetween(@Param("userId") Long userId,
                                                @Param("startDate") OffsetDateTime startDate,
                                                @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT o FROM Order o JOIN FETCH o.status WHERE o.orderId = :orderId AND o.isDeleted = false")
    Optional<Order> findByIdWithStatus(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi WHERE o.orderId = :orderId AND o.isDeleted = false")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    Optional<Order> findByCorrelationIdAndIsDeletedFalse(UUID correlationId);

    Page<Order> findByStatusStatusIdAndIsDeletedFalseOrderByOrderDateDesc(Long statusId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.isDeleted = false")
    long countByUserId(@Param("userId") Long userId);
}