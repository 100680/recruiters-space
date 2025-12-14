package com.ebuy.review.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReviewNotFoundException extends RuntimeException {

    private final String errorCode;

    public ReviewNotFoundException(String message) {
        super(message);
        this.errorCode = "REVIEW_NOT_FOUND";
    }

    public ReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "REVIEW_NOT_FOUND";
    }

    public ReviewNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ReviewNotFoundException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
