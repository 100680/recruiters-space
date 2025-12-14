package com.ebuy.payment.exception;

public class OptimisticLockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long entityId;
    private final String entityType;

    public OptimisticLockException(String entityType, Long entityId) {
        super(String.format("Optimistic lock failure for %s with ID: %d. The entity was modified by another transaction.",
                entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public OptimisticLockException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = null;
        this.entityId = null;
    }

    public OptimisticLockException(String entityType, Long entityId, String customMessage) {
        super(String.format("%s for %s with ID: %d", customMessage, entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }
}