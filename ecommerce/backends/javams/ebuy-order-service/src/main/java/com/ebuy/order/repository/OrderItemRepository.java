package com.ebuy.order.repository;

import com.ebuy.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderIdAndIsDeletedFalseOrderByOrderItemIdAsc(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId AND oi.productId = :productId AND oi.isDeleted = false")
    Optional<OrderItem> findByOrderIdAndProductId(@Param("orderId") Long orderId, @Param("productId") Long productId);

    boolean existsByOrderOrderIdAndProductIdAndIsDeletedFalse(Long orderId, Long productId);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.orderId = :orderId AND oi.isDeleted = false")
    long countByOrderId(@Param("orderId") Long orderId);
}