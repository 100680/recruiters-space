package com.ebuy.order.entity;

import com.ebuy.order.enums.AuditAction;
import com.ebuy.order.enums.AuditLevel;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Audit log entity for tracking all order item operations.
 */
@Entity
@Table(name = "audit_logs", schema = "order_schema",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_timestamp", columnList = "created_at"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_level", columnList = "level")
        })
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private AuditLevel level;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "audit_data", columnDefinition = "TEXT")
    private String auditData;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Constructors
    public AuditLog() {}

    public AuditLog(AuditAction action, AuditLevel level, String entityType, Long entityId,
                    Long userId, String description) {
        this.action = action;
        this.level = level;
        this.entityType = entityType;
        this.entityId = entityId;
        this.userId = userId;
        this.description = description;
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public AuditLevel getLevel() { return level; }
    public void setLevel(AuditLevel level) { this.level = level; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuditData() { return auditData; }
    public void setAuditData(String auditData) { this.auditData = auditData; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}