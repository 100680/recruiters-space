package com.ebuy.cart.service.impl;

import com.ebuy.cart.dto.request.AddToCartRequest;
import com.ebuy.cart.dto.request.UpdateCartItemRequest;
import com.ebuy.cart.dto.response.CartItemResponse;
import com.ebuy.cart.dto.response.CartResponse;
import com.ebuy.cart.exception.CartException;
import com.ebuy.cart.exception.CartItemNotFoundException;
import com.ebuy.cart.model.entity.CartItem;
import com.ebuy.cart.repository.CartItemRepository;
import com.ebuy.cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartItemRepository cartItemRepository;

    @Value("${app.cart.max-items-per-user:100}")
    private int maxItemsPerUser;

    public CartServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartResponse getCart(Long userId, String sessionId) {
        logger.debug("Getting cart for userId: {}, sessionId: {}", userId, sessionId);

        List<CartItem> cartItems;
        if (userId != null) {
            cartItems = cartItemRepository.findActiveCartItemsByUserId(userId);
        } else if (sessionId != null) {
            cartItems = cartItemRepository.findActiveCartItemsBySessionId(sessionId);
        } else {
            throw new CartException("Either userId or sessionId must be provided");
        }

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return new CartResponse(userId, sessionId, itemResponses, itemResponses.size());
    }

    @Override
    public CartItemResponse addToCart(AddToCartRequest request) {
        logger.debug("Adding item to cart: productId={}, quantity={}, userId={}, sessionId={}",
                request.getProductId(), request.getQuantity(), request.getUserId(), request.getSessionId());

        if (request.getUserId() == null && request.getSessionId() == null) {
            throw new CartException("Either userId or sessionId must be provided");
        }

        // Check maximum items limit
        long currentItemCount = request.getUserId() != null
                ? cartItemRepository.countActiveItemsByUserId(request.getUserId())
                : cartItemRepository.countActiveItemsBySessionId(request.getSessionId());

        if (currentItemCount >= maxItemsPerUser) {
            throw new CartException("Maximum cart items limit reached");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = request.getUserId() != null
                ? cartItemRepository.findActiveCartItemByUserIdAndProductId(request.getUserId(), request.getProductId())
                : cartItemRepository.findActiveCartItemBySessionIdAndProductId(request.getSessionId(), request.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Create new cart item
            cartItem = new CartItem(request.getUserId(), request.getSessionId(),
                    request.getProductId(), request.getQuantity());
        }

        cartItem = cartItemRepository.save(cartItem);
        logger.info("Cart item saved: {}", cartItem.getCartItemId());

        return convertToCartItemResponse(cartItem);
    }

    @Override
    public CartItemResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request) {
        logger.debug("Updating cart item: cartItemId={}, quantity={}", cartItemId, request.getQuantity());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + cartItemId));

        if (cartItem.getIsDeleted()) {
            throw new CartItemNotFoundException("Cart item not found with id: " + cartItemId);
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        // Clear cache for this user/session
        String cacheKey = cartItem.getUserId() != null
                ? "user:" + cartItem.getUserId()
                : "session:" + cartItem.getSessionId();
        // Cache eviction will be handled by aspect

        logger.info("Cart item updated: {}", cartItem.getCartItemId());
        return convertToCartItemResponse(cartItem);
    }

    @Override
    public void removeFromCart(Long cartItemId) {
        logger.debug("Removing cart item: cartItemId={}", cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + cartItemId));

        if (cartItem.getIsDeleted()) {
            throw new CartItemNotFoundException("Cart item not found with id: " + cartItemId);
        }

        // Soft delete
        cartItem.setIsDeleted(true);
        cartItem.setDeletedAt(Instant.now());
        cartItemRepository.save(cartItem);

        logger.info("Cart item removed: {}", cartItemId);
    }

    @Override
    public void clearCart(Long userId, String sessionId) {
        logger.debug("Clearing cart for userId: {}, sessionId: {}", userId, sessionId);

        int deletedCount;
        if (userId != null) {
            deletedCount = cartItemRepository.softDeleteAllByUserId(userId, Instant.now());
        } else if (sessionId != null) {
            deletedCount = cartItemRepository.softDeleteAllBySessionId(sessionId, Instant.now());
        } else {
            throw new CartException("Either userId or sessionId must be provided");
        }

        logger.info("Cleared {} items from cart", deletedCount);
    }

    @Override
    @Transactional
    public void mergeSessionCartToUser(String sessionId, Long userId) {
        logger.debug("Merging session cart to user: sessionId={}, userId={}", sessionId, userId);

        List<CartItem> sessionItems = cartItemRepository.findActiveCartItemsBySessionId(sessionId);
        List<CartItem> userItems = cartItemRepository.findActiveCartItemsByUserId(userId);

        for (CartItem sessionItem : sessionItems) {
            Optional<CartItem> existingUserItem = userItems.stream()
                    .filter(item -> item.getProductId().equals(sessionItem.getProductId()))
                    .findFirst();

            if (existingUserItem.isPresent()) {
                // Merge quantities
                CartItem userItem = existingUserItem.get();
                userItem.setQuantity(userItem.getQuantity() + sessionItem.getQuantity());
                cartItemRepository.save(userItem);
            } else {
                // Transfer session item to user
                sessionItem.setUserId(userId);
                sessionItem.setSessionId(null);
                cartItemRepository.save(sessionItem);
            }
        }

        // Clear remaining session items
        cartItemRepository.softDeleteAllBySessionId(sessionId, Instant.now());

        logger.info("Merged {} session items to user cart", sessionItems.size());
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getCartItemId(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getCreatedAt(),
                cartItem.getModifiedAt(),
                cartItem.getRowVersion()
        );
    }
}