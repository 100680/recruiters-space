package com.ebuy.cart.controller;

import com.ebuy.cart.dto.request.AddToCartRequest;
import com.ebuy.cart.dto.request.UpdateCartItemRequest;
import com.ebuy.cart.dto.response.CartItemResponse;
import com.ebuy.cart.dto.response.CartResponse;
import com.ebuy.cart.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {

        logger.debug("GET /cart - userId: {}, sessionId: {}", userId, sessionId);
        CartResponse cart = cartService.getCart(userId, sessionId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        logger.debug("POST /cart/items - {}", request.getProductId());
        CartItemResponse response = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        logger.debug("PUT /cart/items/{} - quantity: {}", cartItemId, request.getQuantity());
        CartItemResponse response = cartService.updateCartItem(cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        logger.debug("DELETE /cart/items/{}", cartItemId);
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {

        logger.debug("DELETE /cart - userId: {}, sessionId: {}", userId, sessionId);
        cartService.clearCart(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeSessionCartToUser(
            @RequestParam String sessionId,
            @RequestParam Long userId) {

        logger.debug("POST /cart/merge - sessionId: {}, userId: {}", sessionId, userId);
        cartService.mergeSessionCartToUser(sessionId, userId);
        return ResponseEntity.ok().build();
    }
}