package com.ebuy.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderAmountException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderAmountException(String message) {
        super(message);
    }

    public InvalidOrderAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
