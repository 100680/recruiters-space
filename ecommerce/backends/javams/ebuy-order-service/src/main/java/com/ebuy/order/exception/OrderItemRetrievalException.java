package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderItemRetrievalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderItemRetrievalException(String message) {
        super(message);
    }

    public OrderItemRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
