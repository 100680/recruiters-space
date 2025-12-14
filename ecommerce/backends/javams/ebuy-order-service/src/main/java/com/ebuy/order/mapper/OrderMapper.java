package com.ebuy.order.mapper;

import com.ebuy.order.dto.OrderDto;
import com.ebuy.order.entity.Order;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for converting between Order entities and OrderDto objects.
 * Provides high-performance mapping with compile-time code generation.
 */
@Mapper(
        componentModel = "spring",
        uses = {OrderStatusMapper.class, OrderItemMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
@Component
public interface OrderMapper {

    /**
     * Converts an Order entity to OrderDto.
     *
     * @param order the Order entity
     * @return the OrderDto
     */
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderItems", target = "orderItems", ignore = true)
    OrderDto toDto(Order order);

    /**
     * Converts an OrderDto to Order entity.
     *
     * @param orderDto the OrderDto
     * @return the Order entity
     */
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderItems", target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    Order toEntity(OrderDto orderDto);

    /**
     * Converts an Order entity to OrderDto with items included.
     *
     * @param order the Order entity
     * @return the OrderDto with items
     */
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderDto toDtoWithItems(Order order);

    /**
     * Converts an Order entity to OrderDto with status included.
     *
     * @param order the Order entity
     * @return the OrderDto with status
     */
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderItems", target = "orderItems", ignore = true)
    OrderDto toDtoWithStatus(Order order);

    /**
     * Converts a list of Order entities to a list of OrderDto objects.
     *
     * @param orders the list of Order entities
     * @return the list of OrderDto objects
     */
    List<OrderDto> toDtoList(List<Order> orders);

    /**
     * Converts a list of OrderDto objects to a list of Order entities.
     *
     * @param orderDtos the list of OrderDto objects
     * @return the list of Order entities
     */
    List<Order> toEntityList(List<OrderDto> orderDtos);

    /**
     * Updates an existing Order entity with data from OrderDto.
     * Only updates non-null values from the DTO.
     *
     * @param orderDto the source OrderDto
     * @param order the target Order entity to update
     */
    @Mapping(source = "userId", target = "userId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "totalAmount", target = "totalAmount", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "serviceOrigin", target = "serviceOrigin", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDto(OrderDto orderDto, @MappingTarget Order order);

    /**
     * Creates a partial OrderDto for summary purposes.
     * Excludes heavy fields like orderItems.
     *
     * @param order the Order entity
     * @return the summary OrderDto
     */
    @Mapping(source = "status.statusName", target = "status.statusName")
    @Mapping(target = "orderItems", ignore = true)
    OrderDto toSummaryDto(Order order);

    /**
     * Custom mapping method for handling correlation ID.
     * Generates a new UUID if not provided.
     */
    @AfterMapping
    default void handleCorrelationId(@MappingTarget Order order, OrderDto orderDto) {
        if (order.getCorrelationId() == null && orderDto.getCorrelationId() == null) {
            order.setCorrelationId(java.util.UUID.randomUUID());
        }
    }

    /**
     * Custom mapping method for handling row version.
     * Sets default value for new entities.
     */
    @AfterMapping
    default void handleRowVersion(@MappingTarget Order order) {
        if (order.getRowVersion() == null) {
            order.setRowVersion(1L);
        }
    }

    /**
     * Custom mapping method for validation during entity creation.
     */
    @BeforeMapping
    default void validateBeforeMapping(OrderDto orderDto) {
        // Custom validation logic can be added here
        if (orderDto != null && orderDto.getTotalAmount() != null &&
                orderDto.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
    }
}