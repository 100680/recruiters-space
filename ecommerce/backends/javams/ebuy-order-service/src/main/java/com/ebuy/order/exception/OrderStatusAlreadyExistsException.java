package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrderStatusAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderStatusAlreadyExistsException(String message) {
        super(message);
    }

    public OrderStatusAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}