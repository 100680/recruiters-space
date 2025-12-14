package com.ebuy.cart.repository;

import com.ebuy.cart.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c WHERE c.userId = :userId AND c.isDeleted = false")
    List<CartItem> findActiveCartItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM CartItem c WHERE c.sessionId = :sessionId AND c.isDeleted = false")
    List<CartItem> findActiveCartItemsBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT c FROM CartItem c WHERE c.userId = :userId AND c.productId = :productId AND c.isDeleted = false")
    Optional<CartItem> findActiveCartItemByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("SELECT c FROM CartItem c WHERE c.sessionId = :sessionId AND c.productId = :productId AND c.isDeleted = false")
    Optional<CartItem> findActiveCartItemBySessionIdAndProductId(@Param("sessionId") String sessionId, @Param("productId") Long productId);

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId AND c.isDeleted = false")
    long countActiveItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.sessionId = :sessionId AND c.isDeleted = false")
    long countActiveItemsBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE CartItem c SET c.isDeleted = true, c.deletedAt = :deletedAt WHERE c.userId = :userId AND c.isDeleted = false")
    int softDeleteAllByUserId(@Param("userId") Long userId, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("UPDATE CartItem c SET c.isDeleted = true, c.deletedAt = :deletedAt WHERE c.sessionId = :sessionId AND c.isDeleted = false")
    int softDeleteAllBySessionId(@Param("sessionId") String sessionId, @Param("deletedAt") Instant deletedAt);
}