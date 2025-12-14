package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderItemCreationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderItemCreationException(String message) {
        super(message);
    }

    public OrderItemCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
