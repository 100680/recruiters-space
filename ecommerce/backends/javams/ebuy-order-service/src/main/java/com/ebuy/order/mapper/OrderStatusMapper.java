package com.ebuy.order.mapper;

import com.ebuy.order.dto.OrderStatusDto;
import com.ebuy.order.entity.OrderStatus;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for converting between OrderStatus entities and OrderStatusDto objects.
 * Provides high-performance mapping with compile-time code generation.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
@Component
public interface OrderStatusMapper {

    /**
     * Converts an OrderStatus entity to OrderStatusDto.
     *
     * @param orderStatus the OrderStatus entity
     * @return the OrderStatusDto
     */
    OrderStatusDto toDto(OrderStatus orderStatus);

    /**
     * Converts an OrderStatusDto to OrderStatus entity.
     *
     * @param orderStatusDto the OrderStatusDto
     * @return the OrderStatus entity
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    OrderStatus toEntity(OrderStatusDto orderStatusDto);

    /**
     * Converts a list of OrderStatus entities to a list of OrderStatusDto objects.
     *
     * @param orderStatuses the list of OrderStatus entities
     * @return the list of OrderStatusDto objects
     */
    List<OrderStatusDto> toDtoList(List<OrderStatus> orderStatuses);

    /**
     * Converts a list of OrderStatusDto objects to a list of OrderStatus entities.
     *
     * @param orderStatusDtos the list of OrderStatusDto objects
     * @return the list of OrderStatus entities
     */
    List<OrderStatus> toEntityList(List<OrderStatusDto> orderStatusDtos);

    /**
     * Updates an existing OrderStatus entity with data from OrderStatusDto.
     * Only updates non-null values from the DTO.
     *
     * @param orderStatusDto the source OrderStatusDto
     * @param orderStatus the target OrderStatus entity to update
     */
    @Mapping(source = "statusName", target = "statusName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    void updateEntityFromDto(OrderStatusDto orderStatusDto, @MappingTarget OrderStatus orderStatus);

    /**
     * Creates a minimal OrderStatusDto for reference purposes.
     * Includes only ID and name for performance.
     *
     * @param orderStatus the OrderStatus entity
     * @return the minimal OrderStatusDto
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    OrderStatusDto toMinimalDto(OrderStatus orderStatus);

    /**
     * Custom mapping method for handling row version.
     * Sets default value for new entities.
     */
    @AfterMapping
    default void handleRowVersion(@MappingTarget OrderStatus orderStatus) {
        if (orderStatus.getRowVersion() == null) {
            orderStatus.setRowVersion(1L);
        }
    }

    /**
     * Custom mapping method for validation during entity creation.
     */
    @BeforeMapping
    default void validateBeforeMapping(OrderStatusDto orderStatusDto) {
        if (orderStatusDto != null && orderStatusDto.getStatusName() != null) {
            String statusName = orderStatusDto.getStatusName().trim();
            if (statusName.isEmpty()) {
                throw new IllegalArgumentException("Status name cannot be empty");
            }

            if (statusName.length() > 50) {
                throw new IllegalArgumentException("Status name cannot exceed 50 characters");
            }

            // Ensure status name is in uppercase for consistency
            orderStatusDto.setStatusName(statusName.toUpperCase());
        }
    }

    /**
     * Custom mapping method for normalizing status name.
     */
    @AfterMapping
    default void normalizeStatusName(@MappingTarget OrderStatus orderStatus, OrderStatusDto orderStatusDto) {
        if (orderStatus.getStatusName() != null) {
            // Ensure consistent formatting: uppercase with underscores
            String normalizedName = orderStatus.getStatusName()
                    .trim()
                    .toUpperCase()
                    .replaceAll("\\s+", "_");
            orderStatus.setStatusName(normalizedName);
        }
    }
}