package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderItemDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderItemDataException(String message) {
        super(message);
    }

    public InvalidOrderItemDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
