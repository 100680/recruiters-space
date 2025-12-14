package com.ebuy.order.mapper;

import com.ebuy.order.dto.OrderItemDto;
import com.ebuy.order.entity.OrderItem;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for converting between OrderItem entities and OrderItemDto objects.
 * Provides high-performance mapping with compile-time code generation.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
@Component
public interface OrderItemMapper {

    /**
     * Converts an OrderItem entity to OrderItemDto.
     *
     * @param orderItem the OrderItem entity
     * @return the OrderItemDto
     */
    @Mapping(source = "order.orderId", target = "orderId")
    OrderItemDto toDto(OrderItem orderItem);

    /**
     * Converts an OrderItemDto to OrderItem entity.
     *
     * @param orderItemDto the OrderItemDto
     * @return the OrderItem entity
     */
    @Mapping(target = "order", ignore = true) // Order will be set separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    OrderItem toEntity(OrderItemDto orderItemDto);

    /**
     * Converts a list of OrderItem entities to a list of OrderItemDto objects.
     *
     * @param orderItems the list of OrderItem entities
     * @return the list of OrderItemDto objects
     */
    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);

    /**
     * Converts a list of OrderItemDto objects to a list of OrderItem entities.
     *
     * @param orderItemDtos the list of OrderItemDto objects
     * @return the list of OrderItem entities
     */
    List<OrderItem> toEntityList(List<OrderItemDto> orderItemDtos);

    /**
     * Updates an existing OrderItem entity with data from OrderItemDto.
     * Only updates non-null values from the DTO.
     *
     * @param orderItemDto the source OrderItemDto
     * @param orderItem the target OrderItem entity to update
     */
    @Mapping(source = "quantity", target = "quantity", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "price", target = "price", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "discountMethodId", target = "discountMethodId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "discountValue", target = "discountValue", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "finalPrice", target = "finalPrice", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", ignore = true) // ProductId should not be changed after creation
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    void updateEntityFromDto(OrderItemDto orderItemDto, @MappingTarget OrderItem orderItem);

    /**
     * Creates a partial OrderItemDto for summary purposes.
     * Includes only essential fields for performance.
     *
     * @param orderItem the OrderItem entity
     * @return the summary OrderItemDto
     */
    @Mapping(source = "order.orderId", target = "orderId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OrderItemDto toSummaryDto(OrderItem orderItem);

    /**
     * Creates an OrderItemDto with minimal fields for listing purposes.
     *
     * @param orderItem the OrderItem entity
     * @return the minimal OrderItemDto
     */
    @Mapping(source = "order.orderId", target = "orderId")
    @Mapping(target = "discountMethodId", ignore = true)
    @Mapping(target = "discountValue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    OrderItemDto toMinimalDto(OrderItem orderItem);

    /**
     * Custom mapping method for handling row version.
     * Sets default value for new entities.
     */
    @AfterMapping
    default void handleRowVersion(@MappingTarget OrderItem orderItem) {
        if (orderItem.getRowVersion() == null) {
            orderItem.setRowVersion(1L);
        }
    }

    /**
     * Custom mapping method for validation during entity creation.
     */
    @BeforeMapping
    default void validateBeforeMapping(OrderItemDto orderItemDto) {
        if (orderItemDto != null) {
            if (orderItemDto.getQuantity() != null && orderItemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            if (orderItemDto.getPrice() != null &&
                    orderItemDto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }

            if (orderItemDto.getFinalPrice() != null &&
                    orderItemDto.getFinalPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Final price cannot be negative");
            }
        }
    }

    /**
     * Custom mapping method for handling calculated fields.
     */
    @AfterMapping
    default void handleCalculatedFields(@MappingTarget OrderItem orderItem, OrderItemDto orderItemDto) {
        // If final price is not provided, calculate it from price and quantity
        if (orderItem.getFinalPrice() == null && orderItem.getPrice() != null && orderItem.getQuantity() != null) {
            java.math.BigDecimal calculatedPrice = orderItem.getPrice()
                    .multiply(new java.math.BigDecimal(orderItem.getQuantity()))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // Apply discount if available
            if (orderItem.getDiscountValue() != null &&
                    orderItem.getDiscountValue().compareTo(java.math.BigDecimal.ZERO) > 0) {
                calculatedPrice = calculatedPrice.subtract(orderItem.getDiscountValue());
            }

            orderItem.setFinalPrice(calculatedPrice.max(java.math.BigDecimal.ZERO));
        }
    }
}