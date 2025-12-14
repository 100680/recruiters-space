package com.ebuy.order.validation;

import com.ebuy.order.dto.OrderItemDto;
import com.ebuy.order.exception.InvalidOrderItemDataException;
import com.ebuy.order.repository.OrderRepository;
import com.ebuy.order.service.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * High-performance validator for OrderItem operations.
 * Implements caching and optimized validation rules for scalability.
 */
@Component
public class OrderItemValidator {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemValidator.class);

    // Cache for validation results to improve performance
    private final ConcurrentMap<String, Boolean> validationCache = new ConcurrentHashMap<>();

    @Value("${ebuy.order.item.min-quantity:1}")
    private Integer minQuantity;

    @Value("${ebuy.order.item.max-quantity:1000}")
    private Integer maxQuantity;

    @Value("${ebuy.order.item.min-price:0.01}")
    private BigDecimal minPrice;

    @Value("${ebuy.order.item.max-price:999999.99}")
    private BigDecimal maxPrice;

    @Value("${ebuy.order.item.max-discount-percentage:100}")
    private BigDecimal maxDiscountPercentage;

    @Value("${ebuy.order.item.validation-cache-ttl:300}")
    private Integer validationCacheTtl;

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderItemValidator(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    /**
     * Validates OrderItemDto for creation operation.
     */
    public void validateForCreation(OrderItemDto orderItemDto) {
        logger.debug("Validating order item for creation: {}", orderItemDto);

        if (orderItemDto == null) {
            throw new InvalidOrderItemDataException("Order item data cannot be null");
        }

        // Basic field validations
        validateRequiredFields(orderItemDto, true);
        validateBusinessRules(orderItemDto);

        // External validations with caching
        validateOrderExists(orderItemDto.getOrderId());
        validateProductExists(orderItemDto.getProductId());
        validateProductAvailability(orderItemDto.getProductId(), orderItemDto.getQuantity());

        // Cross-field validations
        validatePriceConsistency(orderItemDto);
        validateDiscountRules(orderItemDto);

        logger.debug("Order item validation for creation completed successfully");
    }

    /**
     * Validates OrderItemDto for update operation.
     */
    public void validateForUpdate(OrderItemDto orderItemDto) {
        logger.debug("Validating order item for update: {}", orderItemDto);

        if (orderItemDto == null) {
            throw new InvalidOrderItemDataException("Order item data cannot be null");
        }

        // Basic field validations (ID required for update)
        validateRequiredFields(orderItemDto, false);
        validateBusinessRules(orderItemDto);

        // Product availability check with current item consideration
        if (orderItemDto.getProductId() != null) {
            validateProductExists(orderItemDto.getProductId());
            validateProductAvailabilityForUpdate(orderItemDto.getOrderItemId(),
                    orderItemDto.getProductId(),
                    orderItemDto.getQuantity());
        }

        // Cross-field validations
        validatePriceConsistency(orderItemDto);
        validateDiscountRules(orderItemDto);

        // Version control validation
        validateRowVersion(orderItemDto);

        logger.debug("Order item validation for update completed successfully");
    }

    /**
     * Validates bulk update operation.
     */
    public void validateBulkUpdate(List<OrderItemDto> orderItemDtos) {
        logger.debug("Validating bulk update for {} order items",
                orderItemDtos != null ? orderItemDtos.size() : 0);

        if (orderItemDtos == null || orderItemDtos.isEmpty()) {
            throw new InvalidOrderItemDataException("Order items list cannot be null or empty");
        }

        if (orderItemDtos.size() > 100) {
            throw new InvalidOrderItemDataException("Bulk update exceeds maximum allowed size: 100");
        }

        // Validate each item
        for (int i = 0; i < orderItemDtos.size(); i++) {
            try {
                validateForUpdate(orderItemDtos.get(i));
            } catch (InvalidOrderItemDataException e) {
                throw new InvalidOrderItemDataException(
                        String.format("Validation failed for item at index %d: %s", i, e.getMessage()), e);
            }
        }

        // Check for duplicates
        long distinctCount = orderItemDtos.stream()
                .filter(Objects::nonNull)
                .map(OrderItemDto::getOrderItemId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        if (distinctCount != orderItemDtos.size()) {
            throw new InvalidOrderItemDataException("Duplicate order item IDs found in bulk update");
        }

        logger.debug("Bulk update validation completed successfully");
    }

    // Private validation methods

    private void validateRequiredFields(OrderItemDto orderItemDto, boolean isCreation) {
        if (isCreation) {
            if (orderItemDto.getOrderId() == null) {
                throw new InvalidOrderItemDataException("Order ID is required");
            }
            if (orderItemDto.getProductId() == null) {
                throw new InvalidOrderItemDataException("Product ID is required");
            }
        } else {
            if (orderItemDto.getOrderItemId() == null) {
                throw new InvalidOrderItemDataException("Order Item ID is required for update");
            }
        }

        if (orderItemDto.getQuantity() == null) {
            throw new InvalidOrderItemDataException("Quantity is required");
        }

        if (orderItemDto.getPrice() == null) {
            throw new InvalidOrderItemDataException("Price is required");
        }

        if (orderItemDto.getFinalPrice() == null) {
            throw new InvalidOrderItemDataException("Final price is required");
        }
    }

    private void validateBusinessRules(OrderItemDto orderItemDto) {
        // Quantity validation
        if (orderItemDto.getQuantity() < minQuantity || orderItemDto.getQuantity() > maxQuantity) {
            throw new InvalidOrderItemDataException(
                    String.format("Quantity must be between %d and %d", minQuantity, maxQuantity));
        }

        // Price validation
        if (orderItemDto.getPrice().compareTo(minPrice) < 0 ||
                orderItemDto.getPrice().compareTo(maxPrice) > 0) {
            throw new InvalidOrderItemDataException(
                    String.format("Price must be between %s and %s", minPrice, maxPrice));
        }

        // Final price validation
        if (orderItemDto.getFinalPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderItemDataException("Final price cannot be negative");
        }

        if (orderItemDto.getFinalPrice().compareTo(maxPrice) > 0) {
            throw new InvalidOrderItemDataException(
                    "Final price exceeds maximum allowed: " + maxPrice);
        }

        // Discount value validation
        if (orderItemDto.getDiscountValue() != null) {
            if (orderItemDto.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderItemDataException("Discount value cannot be negative");
            }

            if (orderItemDto.getDiscountValue().compareTo(orderItemDto.getPrice()) > 0) {
                throw new InvalidOrderItemDataException("Discount value cannot exceed item price");
            }
        }
    }

    @Cacheable(value = "orderValidation", key = "#orderId", unless = "#result == false")
    private void validateOrderExists(Long orderId) {
        String cacheKey = "order_exists_" + orderId;

        if (validationCache.containsKey(cacheKey)) {
            if (!validationCache.get(cacheKey)) {
                throw new InvalidOrderItemDataException("Order not found: " + orderId);
            }
            return;
        }

        boolean exists = orderRepository.existsById(orderId);
        validationCache.put(cacheKey, exists);

        if (!exists) {
            throw new InvalidOrderItemDataException("Order not found: " + orderId);
        }
    }

    @Cacheable(value = "productValidation", key = "#productId", unless = "#result == false")
    private void validateProductExists(Long productId) {
        String cacheKey = "product_exists_" + productId;

        if (validationCache.containsKey(cacheKey)) {
            if (!validationCache.get(cacheKey)) {
                throw new InvalidOrderItemDataException("Product not found: " + productId);
            }
            return;
        }

        boolean exists = productService.existsById(productId);
        validationCache.put(cacheKey, exists);

        if (!exists) {
            throw new InvalidOrderItemDataException("Product not found: " + productId);
        }
    }

    private void validateProductAvailability(Long productId, Integer requestedQuantity) {
        try {
            Integer availableStock = productService.getAvailableStock(productId);

            if (availableStock == null || availableStock < requestedQuantity) {
                throw new InvalidOrderItemDataException(
                        String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                                productId, availableStock, requestedQuantity));
            }
        } catch (Exception e) {
            logger.error("Error validating product availability for product: {}", productId, e);
            throw new InvalidOrderItemDataException(
                    "Unable to validate product availability: " + productId, e);
        }
    }

    private void validateProductAvailabilityForUpdate(Long orderItemId, Long productId, Integer requestedQuantity) {
        try {
            // Get current quantity to calculate net change
            Integer currentQuantity = getCurrentOrderItemQuantity(orderItemId);
            Integer quantityChange = requestedQuantity - (currentQuantity != null ? currentQuantity : 0);

            if (quantityChange > 0) {
                Integer availableStock = productService.getAvailableStock(productId);

                if (availableStock == null || availableStock < quantityChange) {
                    throw new InvalidOrderItemDataException(
                            String.format("Insufficient additional stock for product %d. Available: %d, Additional needed: %d",
                                    productId, availableStock, quantityChange));
                }
            }
        } catch (Exception e) {
            logger.error("Error validating product availability for update. OrderItem: {}, Product: {}",
                    orderItemId, productId, e);
            throw new InvalidOrderItemDataException(
                    "Unable to validate product availability for update", e);
        }
    }

    private Integer getCurrentOrderItemQuantity(Long orderItemId) {
        // This would typically be injected as a repository method
        // For now, returning null to indicate unknown current quantity
        return null;
    }

    private void validatePriceConsistency(OrderItemDto orderItemDto) {
        BigDecimal calculatedTotal = orderItemDto.getPrice()
                .multiply(BigDecimal.valueOf(orderItemDto.getQuantity()));

        BigDecimal expectedFinalPrice = orderItemDto.getDiscountValue() != null ?
                calculatedTotal.subtract(orderItemDto.getDiscountValue()) : calculatedTotal;

        // Allow for small rounding differences
        BigDecimal difference = orderItemDto.getFinalPrice().subtract(expectedFinalPrice).abs();
        BigDecimal tolerance = BigDecimal.valueOf(0.01);

        if (difference.compareTo(tolerance) > 0) {
            throw new InvalidOrderItemDataException(
                    String.format("Price inconsistency detected. Expected: %s, Actual: %s",
                            expectedFinalPrice, orderItemDto.getFinalPrice()));
        }
    }

    private void validateDiscountRules(OrderItemDto orderItemDto) {
        if (orderItemDto.getDiscountValue() == null) {
            return;
        }

        if (orderItemDto.getDiscountMethodId() == null) {
            throw new InvalidOrderItemDataException(
                    "Discount method ID is required when discount value is specified");
        }

        // Validate discount percentage doesn't exceed maximum
        BigDecimal totalPrice = orderItemDto.getPrice()
                .multiply(BigDecimal.valueOf(orderItemDto.getQuantity()));

        if (totalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountPercentage = orderItemDto.getDiscountValue()
                    .divide(totalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (discountPercentage.compareTo(maxDiscountPercentage) > 0) {
                throw new InvalidOrderItemDataException(
                        String.format("Discount percentage %.2f%% exceeds maximum allowed: %.2f%%",
                                discountPercentage, maxDiscountPercentage));
            }
        }
    }

    private void validateRowVersion(OrderItemDto orderItemDto) {
        if (orderItemDto.getRowVersion() == null) {
            throw new InvalidOrderItemDataException("Row version is required for update operations");
        }

        if (orderItemDto.getRowVersion() <= 0) {
            throw new InvalidOrderItemDataException("Row version must be positive");
        }
    }

    /**
     * Clears the validation cache. Should be called periodically or when cache becomes stale.
     */
    public void clearValidationCache() {
        validationCache.clear();
        logger.info("Validation cache cleared");
    }

    /**
     * Gets the current size of the validation cache.
     */
    public int getValidationCacheSize() {
        return validationCache.size();
    }
}