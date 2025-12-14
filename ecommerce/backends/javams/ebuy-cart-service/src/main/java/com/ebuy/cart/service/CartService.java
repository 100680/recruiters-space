package com.ebuy.cart.service;

import com.ebuy.cart.dto.request.AddToCartRequest;
import com.ebuy.cart.dto.request.UpdateCartItemRequest;
import com.ebuy.cart.dto.response.CartResponse;
import com.ebuy.cart.dto.response.CartItemResponse;

public interface CartService {
    CartResponse getCart(Long userId, String sessionId);
    CartItemResponse addToCart(AddToCartRequest request);
    CartItemResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request);
    void removeFromCart(Long cartItemId);
    void clearCart(Long userId, String sessionId);
    void mergeSessionCartToUser(String sessionId, Long userId);
}
