package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderItemUpdateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderItemUpdateException(String message) {
        super(message);
    }

    public OrderItemUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
