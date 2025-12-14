package com.ebuy.payment.exception;


public final class ErrorCode {

    private ErrorCode() {
        // Private constructor to prevent instantiation
    }

    // Payment related error codes
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_001";
    public static final String PAYMENT_ALREADY_EXISTS = "PAYMENT_002";
    public static final String PAYMENT_PROCESSING_FAILED = "PAYMENT_003";
    public static final String PAYMENT_AMOUNT_INVALID = "PAYMENT_004";
    public static final String PAYMENT_STATUS_INVALID = "PAYMENT_005";
    public static final String PAYMENT_ALREADY_PROCESSED = "PAYMENT_006";
    public static final String PAYMENT_ALREADY_REFUNDED = "PAYMENT_007";
    public static final String PAYMENT_CANNOT_BE_VOIDED = "PAYMENT_008";

    // Payment Method Type related error codes
    public static final String PAYMENT_METHOD_TYPE_NOT_FOUND = "PMT_001";
    public static final String PAYMENT_METHOD_TYPE_ALREADY_EXISTS = "PMT_002";
    public static final String PAYMENT_METHOD_TYPE_IN_USE = "PMT_003";

    // Order related error codes
    public static final String ORDER_NOT_FOUND = "ORDER_001";
    public static final String ORDER_ALREADY_PAID = "ORDER_002";
    public static final String ORDER_PAYMENT_MISMATCH = "ORDER_003";

    // Validation error codes
    public static final String VALIDATION_ERROR = "VAL_001";
    public static final String CONSTRAINT_VIOLATION = "VAL_002";
    public static final String INVALID_INPUT = "VAL_003";
    public static final String MISSING_REQUIRED_FIELD = "VAL_004";

    // Database error codes
    public static final String OPTIMISTIC_LOCK_ERROR = "DB_001";
    public static final String DATA_INTEGRITY_VIOLATION = "DB_002";
    public static final String DATABASE_ERROR = "DB_003";
    public static final String DUPLICATE_KEY_ERROR = "DB_004";

    // System error codes
    public static final String INTERNAL_SERVER_ERROR = "SYS_001";
    public static final String SERVICE_UNAVAILABLE = "SYS_002";
    public static final String TIMEOUT_ERROR = "SYS_003";
    public static final String CONFIGURATION_ERROR = "SYS_004";

    // External service error codes
    public static final String PAYMENT_GATEWAY_ERROR = "EXT_001";
    public static final String PAYMENT_GATEWAY_TIMEOUT = "EXT_002";
    public static final String PAYMENT_GATEWAY_UNAVAILABLE = "EXT_003";

    // Security error codes
    public static final String UNAUTHORIZED = "SEC_001";
    public static final String FORBIDDEN = "SEC_002";
    public static final String AUTHENTICATION_FAILED = "SEC_003";
}
