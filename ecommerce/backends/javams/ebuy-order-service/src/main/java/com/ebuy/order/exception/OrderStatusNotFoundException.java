package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderStatusNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderStatusNotFoundException(String message) {
        super(message);
    }

    public OrderStatusNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
