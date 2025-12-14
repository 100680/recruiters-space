package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderRetrievalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderRetrievalException(String message) {
        super(message);
    }

    public OrderRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

