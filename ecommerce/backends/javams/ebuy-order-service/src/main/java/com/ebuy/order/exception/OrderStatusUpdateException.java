package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderStatusUpdateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderStatusUpdateException(String message) {
        super(message);
    }

    public OrderStatusUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
