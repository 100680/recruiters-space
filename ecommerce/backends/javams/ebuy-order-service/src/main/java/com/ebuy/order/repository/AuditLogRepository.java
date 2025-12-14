package com.ebuy.order.repository;

import com.ebuy.order.entity.AuditLog;
import com.ebuy.order.enums.AuditLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, int limit);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, int limit);

    List<AuditLog> findByLevelOrderByCreatedAtDesc(AuditLevel level, int limit);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :fromDate")
    long countByCreatedAtAfter(@Param("fromDate") OffsetDateTime fromDate);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") OffsetDateTime cutoffDate);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId " +
            "AND a.level IN :levels ORDER BY a.createdAt DESC")
    List<AuditLog> findCriticalAuditLogs(@Param("entityType") String entityType,
                                         @Param("entityId") Long entityId,
                                         @Param("levels") List<AuditLevel> levels,
                                         int limit);
}
