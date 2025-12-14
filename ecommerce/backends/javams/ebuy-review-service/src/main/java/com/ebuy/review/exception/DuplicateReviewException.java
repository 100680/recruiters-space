package com.ebuy.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateReviewException extends RuntimeException {

    private final String errorCode;
    private final Long userId;
    private final Long productId;

    public DuplicateReviewException(String message) {
        super(message);
        this.errorCode = "DUPLICATE_REVIEW";
        this.userId = null;
        this.productId = null;
    }

    public DuplicateReviewException(String message, Long userId, Long productId) {
        super(message);
        this.errorCode = "DUPLICATE_REVIEW";
        this.userId = userId;
        this.productId = productId;
    }

    public DuplicateReviewException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DUPLICATE_REVIEW";
        this.userId = null;
        this.productId = null;
    }

    public DuplicateReviewException(String message, String errorCode, Long userId, Long productId) {
        super(message);
        this.errorCode = errorCode;
        this.userId = userId;
        this.productId = productId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }
}
