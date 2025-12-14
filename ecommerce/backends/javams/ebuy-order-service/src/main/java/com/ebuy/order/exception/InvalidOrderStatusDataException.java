package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderStatusDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderStatusDataException(String message) {
        super(message);
    }

    public InvalidOrderStatusDataException(String message, Throwable cause) {
        super(message, cause);
    }
}