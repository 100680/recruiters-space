package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OrderModificationNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderModificationNotAllowedException(String message) {
        super(message);
    }

    public OrderModificationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
