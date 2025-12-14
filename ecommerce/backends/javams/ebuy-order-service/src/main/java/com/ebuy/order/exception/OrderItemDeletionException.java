package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderItemDeletionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderItemDeletionException(String message) {
        super(message);
    }

    public OrderItemDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
