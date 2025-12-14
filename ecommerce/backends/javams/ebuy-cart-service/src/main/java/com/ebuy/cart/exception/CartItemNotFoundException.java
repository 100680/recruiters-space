package com.ebuy.cart.exception;

public class CartItemNotFoundException extends CartException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}