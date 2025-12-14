package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OrderCancellationNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderCancellationNotAllowedException(String message) {
        super(message);
    }

    public OrderCancellationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
