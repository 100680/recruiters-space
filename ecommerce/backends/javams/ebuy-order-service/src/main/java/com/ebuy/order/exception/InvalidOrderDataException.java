package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderDataException(String message) {
        super(message);
    }

    public InvalidOrderDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
