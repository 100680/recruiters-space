package com.ebuy.order.event.model;

import com.ebuy.order.dto.OrderItemDto;
import java.time.OffsetDateTime;

/**
 * Order item event model for event-driven architecture.
 */
public class OrderItemEvent {
    private String eventId;
    private String eventType;
    private OrderItemDto orderItem;
    private Long userId;
    private OffsetDateTime timestamp;
    private EventMetadata metadata;

    // Constructors
    public OrderItemEvent() {}

    public OrderItemEvent(String eventType, OrderItemDto orderItem, Long userId) {
        this.eventType = eventType;
        this.orderItem = orderItem;
        this.userId = userId;
        this.timestamp = OffsetDateTime.now();
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public OrderItemDto getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItemDto orderItem) { this.orderItem = orderItem; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public EventMetadata getMetadata() { return metadata; }
    public void setMetadata(EventMetadata metadata) { this.metadata = metadata; }
}