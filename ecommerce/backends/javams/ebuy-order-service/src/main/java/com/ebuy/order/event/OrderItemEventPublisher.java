package com.ebuy.order.event;

import com.ebuy.order.dto.OrderItemDto;
import com.ebuy.order.event.model.OrderItemEvent;
import com.ebuy.order.event.model.EventMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * High-performance event publisher for order item operations.
 * Supports multiple event channels: Kafka, local Spring events, and webhooks.
 * Designed for reliability with retry mechanisms and failure handling.
 */
@Service
public class OrderItemEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemEventPublisher.class);

    @Value("${ebuy.events.enabled:true}")
    private boolean eventsEnabled;

    @Value("${ebuy.events.kafka.enabled:true}")
    private boolean kafkaEnabled;

    @Value("${ebuy.events.local.enabled:true}")
    private boolean localEventsEnabled;

    @Value("${ebuy.events.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${ebuy.events.kafka.topic.order-item-events:order-item-events}")
    private String orderItemEventsTopic;

    @Value("${ebuy.events.kafka.topic.inventory-events:inventory-events}")
    private String inventoryEventsTopic;

    @Value("${ebuy.events.kafka.topic.pricing-events:pricing-events}")
    private String pricingEventsTopic;

    @Value("${ebuy.events.async:true}")
    private boolean asyncPublishing;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;
    private final WebhookService webhookService;

    @Autowired
    public OrderItemEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                   ApplicationEventPublisher applicationEventPublisher,
                                   ObjectMapper objectMapper,
                                   WebhookService webhookService) {
        this.kafkaTemplate = kafkaTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
        this.webhookService = webhookService;
    }

    /**
     * Publishes an order item event to all configured channels.
     */
    @Async("eventExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public CompletableFuture<Void> publishOrderItemEvent(String eventType,
                                                         OrderItemDto orderItemDto,
                                                         Long userId) {
        if (!eventsEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Publishing order item event: {} for item: {}", eventType, orderItemDto.getOrderItemId());

        try {
            OrderItemEvent event = createOrderItemEvent(eventType, orderItemDto, userId);

            // Publish to different channels
            CompletableFuture<Void> kafkaFuture = publishToKafka(event);
            CompletableFuture<Void> localFuture = publishToLocalEvents(event);
            CompletableFuture<Void> webhookFuture = publishToWebhooks(event);

            // Wait for all publishing attempts to complete
            return CompletableFuture.allOf(kafkaFuture, localFuture, webhookFuture)
                    .thenRun(() -> logger.info("Successfully published order item event: {} for item: {}",
                            eventType, orderItemDto.getOrderItemId()))
                    .exceptionally(throwable -> {
                        logger.error("Error publishing order item event: {} for item: {}",
                                eventType, orderItemDto.getOrderItemId(), throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to create order item event: {} for item: {}",
                    eventType, orderItemDto.getOrderItemId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publishes inventory-related events when order items change.
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> publishInventoryEvent(String eventType,
                                                         OrderItemDto orderItemDto,
                                                         Integer quantityDelta,
                                                         Long userId) {
        if (!eventsEnabled || !kafkaEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Publishing inventory event: {} for product: {} with delta: {}",
                eventType, orderItemDto.getProductId(), quantityDelta);

        try {
            InventoryEvent inventoryEvent = new InventoryEvent();
            inventoryEvent.setEventId(UUID.randomUUID().toString());
            inventoryEvent.setEventType(eventType);
            inventoryEvent.setProductId(orderItemDto.getProductId());
            inventoryEvent.setOrderId(orderItemDto.getOrderId());
            inventoryEvent.setOrderItemId(orderItemDto.getOrderItemId());
            inventoryEvent.setQuantityDelta(quantityDelta);
            inventoryEvent.setUserId(userId);
            inventoryEvent.setTimestamp(OffsetDateTime.now());
            inventoryEvent.setMetadata(createEventMetadata());

            return publishToKafkaAsync(inventoryEventsTopic, inventoryEvent)
                    .thenRun(() -> logger.info("Successfully published inventory event: {}", eventType))
                    .exceptionally(throwable -> {
                        logger.error("Error publishing inventory event: {}", eventType, throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to create inventory event: {}", eventType, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publishes pricing-related events when order item prices change.
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> publishPricingEvent(String eventType,
                                                       OrderItemDto oldOrderItem,
                                                       OrderItemDto newOrderItem,
                                                       Long userId) {
        if (!eventsEnabled || !kafkaEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Publishing pricing event: {} for order item: {}",
                eventType, newOrderItem.getOrderItemId());

        try {
            PricingEvent pricingEvent = new PricingEvent();
            pricingEvent.setEventId(UUID.randomUUID().toString());
            pricingEvent.setEventType(eventType);
            pricingEvent.setOrderItemId(newOrderItem.getOrderItemId());
            pricingEvent.setProductId(newOrderItem.getProductId());
            pricingEvent.setOldPrice(oldOrderItem != null ? oldOrderItem.getPrice() : null);
            pricingEvent.setNewPrice(newOrderItem.getPrice());
            pricingEvent.setOldFinalPrice(oldOrderItem != null ? oldOrderItem.getFinalPrice() : null);
            pricingEvent.setNewFinalPrice(newOrderItem.getFinalPrice());
            pricingEvent.setUserId(userId);
            pricingEvent.setTimestamp(OffsetDateTime.now());
            pricingEvent.setMetadata(createEventMetadata());

            return publishToKafkaAsync(pricingEventsTopic, pricingEvent)
                    .thenRun(() -> logger.info("Successfully published pricing event: {}", eventType))
                    .exceptionally(throwable -> {
                        logger.error("Error publishing pricing event: {}", eventType, throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to create pricing event: {}", eventType, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publishes bulk operation events.
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> publishBulkOperationEvent(String eventType,
                                                             java.util.List<OrderItemDto> orderItems,
                                                             Long userId,
                                                             boolean success,
                                                             String errorMessage) {
        if (!eventsEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Publishing bulk operation event: {} for {} items", eventType, orderItems.size());

        try {
            BulkOperationEvent bulkEvent = new BulkOperationEvent();
            bulkEvent.setEventId(UUID.randomUUID().toString());
            bulkEvent.setEventType(eventType);
            bulkEvent.setItemCount(orderItems.size());
            bulkEvent.setOrderItemIds(orderItems.stream()
                    .map(OrderItemDto::getOrderItemId)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList()));
            bulkEvent.setSuccess(success);
            bulkEvent.setErrorMessage(errorMessage);
            bulkEvent.setUserId(userId);
            bulkEvent.setTimestamp(OffsetDateTime.now());
            bulkEvent.setMetadata(createEventMetadata());

            CompletableFuture<Void> kafkaFuture = publishToKafkaAsync(orderItemEventsTopic, bulkEvent);
            CompletableFuture<Void> localFuture = publishLocalEventAsync(bulkEvent);

            return CompletableFuture.allOf(kafkaFuture, localFuture)
                    .thenRun(() -> logger.info("Successfully published bulk operation event: {}", eventType))
                    .exceptionally(throwable -> {
                        logger.error("Error publishing bulk operation event: {}", eventType, throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to create bulk operation event: {}", eventType, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publishes events for external system integration.
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> publishIntegrationEvent(String systemName,
                                                           String eventType,
                                                           OrderItemDto orderItemDto,
                                                           java.util.Map<String, Object> additionalData) {
        if (!eventsEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        logger.info("Publishing integration event: {} for system: {}", eventType, systemName);

        try {
            IntegrationEvent integrationEvent = new IntegrationEvent();
            integrationEvent.setEventId(UUID.randomUUID().toString());
            integrationEvent.setSystemName(systemName);
            integrationEvent.setEventType(eventType);
            integrationEvent.setOrderItem(orderItemDto);
            integrationEvent.setAdditionalData(additionalData);
            integrationEvent.setTimestamp(OffsetDateTime.now());
            integrationEvent.setMetadata(createEventMetadata());

            // Use dedicated topic for integration events
            String integrationTopic = "integration-events-" + systemName.toLowerCase();

            return publishToKafkaAsync(integrationTopic, integrationEvent)
                    .thenRun(() -> logger.info("Successfully published integration event: {} for system: {}",
                            eventType, systemName))
                    .exceptionally(throwable -> {
                        logger.error("Error publishing integration event: {} for system: {}",
                                eventType, systemName, throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to create integration event: {} for system: {}", eventType, systemName, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // Private helper methods

    private OrderItemEvent createOrderItemEvent(String eventType, OrderItemDto orderItemDto, Long userId) {
        OrderItemEvent event = new OrderItemEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setOrderItem(orderItemDto);
        event.setUserId(userId);
        event.setTimestamp(OffsetDateTime.now());
        event.setMetadata(createEventMetadata());

        return event;
    }

    private EventMetadata createEventMetadata() {
        EventMetadata metadata = new EventMetadata();
        metadata.setSource("order-service");
        metadata.setVersion("1.0");
        metadata.setCorrelationId(UUID.randomUUID().toString());
        metadata.setOrigin("ebuy-platform");

        return metadata;
    }

    private CompletableFuture<Void> publishToKafka(OrderItemEvent event) {
        if (!kafkaEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        return publishToKafkaAsync(orderItemEventsTopic, event);
    }

    private CompletableFuture<Void> publishToKafkaAsync(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String partitionKey = extractPartitionKey(event);

            return kafkaTemplate.send(topic, partitionKey, eventJson)
                    .completable()
                    .thenRun(() -> logger.debug("Successfully sent event to Kafka topic: {}", topic))
                    .exceptionally(throwable -> {
                        logger.error("Failed to send event to Kafka topic: {}", topic, throwable);
                        return null;
                    });

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event for Kafka", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> publishToLocalEvents(OrderItemEvent event) {
        if (!localEventsEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        return publishLocalEventAsync(event);
    }

    private CompletableFuture<Void> publishLocalEventAsync(Object event) {
        return CompletableFuture.runAsync(() -> {
            try {
                applicationEventPublisher.publishEvent(event);
                logger.debug("Successfully published local event");
            } catch (Exception e) {
                logger.error("Failed to publish local event", e);
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> publishToWebhooks(OrderItemEvent event) {
        if (!webhookEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                webhookService.sendWebhook(event);
                logger.debug("Successfully sent webhook event");
            } catch (Exception e) {
                logger.error("Failed to send webhook event", e);
                throw new RuntimeException(e);
            }
        });
    }

    private String extractPartitionKey(Object event) {
        // Extract appropriate partition key based on event type
        if (event instanceof OrderItemEvent) {
            OrderItemEvent orderItemEvent = (OrderItemEvent) event;
            return orderItemEvent.getOrderItem().getOrderId().toString();
        } else if (event instanceof InventoryEvent) {
            InventoryEvent inventoryEvent = (InventoryEvent) event;
            return inventoryEvent.getProductId().toString();
        } else if (event instanceof PricingEvent) {
            PricingEvent pricingEvent = (PricingEvent) event;
            return pricingEvent.getProductId().toString();
        }

        return UUID.randomUUID().toString();
    }

    // Event model classes would be defined in separate files
    // Including them here for completeness

    public static class InventoryEvent {
        private String eventId;
        private String eventType;
        private Long productId;
        private Long orderId;
        private Long orderItemId;
        private Integer quantityDelta;
        private Long userId;
        private OffsetDateTime timestamp;
        private EventMetadata metadata;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public Long getOrderItemId() { return orderItemId; }
        public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

        public Integer getQuantityDelta() { return quantityDelta; }
        public void setQuantityDelta(Integer quantityDelta) { this.quantityDelta = quantityDelta; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

        public EventMetadata getMetadata() { return metadata; }
        public void setMetadata(EventMetadata metadata) { this.metadata = metadata; }
    }

    public static class PricingEvent {
        private String eventId;
        private String eventType;
        private Long orderItemId;
        private Long productId;
        private java.math.BigDecimal oldPrice;
        private java.math.BigDecimal newPrice;
        private java.math.BigDecimal oldFinalPrice;
        private java.math.BigDecimal newFinalPrice;
        private Long userId;
        private OffsetDateTime timestamp;
        private EventMetadata metadata;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Long getOrderItemId() { return orderItemId; }
        public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public java.math.BigDecimal getOldPrice() { return oldPrice; }
        public void setOldPrice(java.math.BigDecimal oldPrice) { this.oldPrice = oldPrice; }

        public java.math.BigDecimal getNewPrice() { return newPrice; }
        public void setNewPrice(java.math.BigDecimal newPrice) { this.newPrice = newPrice; }

        public java.math.BigDecimal getOldFinalPrice() { return oldFinalPrice; }
        public void setOldFinalPrice(java.math.BigDecimal oldFinalPrice) { this.oldFinalPrice = oldFinalPrice; }

        public java.math.BigDecimal getNewFinalPrice() { return newFinalPrice; }
        public void setNewFinalPrice(java.math.BigDecimal newFinalPrice) { this.newFinalPrice = newFinalPrice; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

        public EventMetadata getMetadata() { return metadata; }
        public void setMetadata(EventMetadata metadata) { this.metadata = metadata; }
    }

    public static class BulkOperationEvent {
        private String eventId;
        private String eventType;
        private Integer itemCount;
        private java.util.List<Long> orderItemIds;
        private Boolean success;
        private String errorMessage;
        private Long userId;
        private OffsetDateTime timestamp;
        private EventMetadata metadata;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Integer getItemCount() { return itemCount; }
        public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

        public java.util.List<Long> getOrderItemIds() { return orderItemIds; }
        public void setOrderItemIds(java.util.List<Long> orderItemIds) { this.orderItemIds = orderItemIds; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

        public EventMetadata getMetadata() { return metadata; }
        public void setMetadata(EventMetadata metadata) { this.metadata = metadata; }
    }

    public static class IntegrationEvent {
        private String eventId;
        private String systemName;
        private String eventType;
        private OrderItemDto orderItem;
        private java.util.Map<String, Object> additionalData;
        private OffsetDateTime timestamp;
        private EventMetadata metadata;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public OrderItemDto getOrderItem() { return orderItem; }
        public void setOrderItem(OrderItemDto orderItem) { this.orderItem = orderItem; }

        public java.util.Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(java.util.Map<String, Object> additionalData) { this.additionalData = additionalData; }

        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

        public EventMetadata getMetadata() { return metadata; }
        public void setMetadata(EventMetadata metadata) { this.metadata = metadata; }
    }

    // Webhook service interface - implement based on your webhook requirements
    public interface WebhookService {
        void sendWebhook(Object event);
    }
}