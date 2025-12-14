package com.ebuy.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global exception handler for the eBuy Order Management service.
 * Provides centralized exception handling with proper HTTP status codes,
 * structured error responses, and comprehensive logging for monitoring.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    // Order-specific exceptions

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        log.warn("Order not found: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "ORDER_NOT_FOUND",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<ErrorResponse> handleOrderCreation(OrderCreationException ex, WebRequest request) {
        log.error("Order creation failed: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "ORDER_CREATION_FAILED",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(OrderUpdateException.class)
    public ResponseEntity<ErrorResponse> handleOrderUpdate(OrderUpdateException ex, WebRequest request) {
        log.error("Order update failed: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "ORDER_UPDATE_FAILED",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(OrderModificationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleOrderModificationNotAllowed(
            OrderModificationNotAllowedException ex, WebRequest request) {
        log.warn("Order modification not allowed: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ORDER_MODIFICATION_NOT_ALLOWED",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidOrderDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderData(InvalidOrderDataException ex, WebRequest request) {
        log.warn("Invalid order data: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ORDER_DATA",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Order Item-specific exceptions

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderItemNotFound(OrderItemNotFoundException ex, WebRequest request) {
        log.warn("Order item not found: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "ORDER_ITEM_NOT_FOUND",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderItemCreationException.class)
    public ResponseEntity<ErrorResponse> handleOrderItemCreation(OrderItemCreationException ex, WebRequest request) {
        log.error("Order item creation failed: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "ORDER_ITEM_CREATION_FAILED",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidOrderItemDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderItemData(
            InvalidOrderItemDataException ex, WebRequest request) {
        log.warn("Invalid order item data: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ORDER_ITEM_DATA",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Order Status-specific exceptions

    @ExceptionHandler(OrderStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderStatusNotFound(OrderStatusNotFoundException ex, WebRequest request) {
        log.warn("Order status not found: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "ORDER_STATUS_NOT_FOUND",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderStatusAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleOrderStatusAlreadyExists(
            OrderStatusAlreadyExistsException ex, WebRequest request) {
        log.warn("Order status already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT.value(),
                "ORDER_STATUS_ALREADY_EXISTS",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOrderStatusDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatusData(
            InvalidOrderStatusDataException ex, WebRequest request) {
        log.warn("Invalid order status data: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ORDER_STATUS_DATA",
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Validation exceptions

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        BindingResult result = ex.getBindingResult();
        Map<String, String> validationErrors = new HashMap<>();

        for (FieldError error : result.getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request
        );
        errorResponse.setValidationErrors(validationErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        Map<String, String> validationErrors = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "CONSTRAINT_VIOLATION",
                "Request constraint validation failed",
                request
        );
        errorResponse.setValidationErrors(validationErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Database and concurrency exceptions

    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(Exception ex, WebRequest request) {
        log.warn("Optimistic locking conflict: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT.value(),
                "OPTIMISTIC_LOCK_CONFLICT",
                "Resource was modified by another user. Please refresh and try again.",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                "Data integrity constraint violated",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Security exceptions

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "Access denied: insufficient permissions",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // HTTP-specific exceptions

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_SUPPORTED",
                "HTTP method not supported: " + ex.getMethod(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("Media type not supported: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "MEDIA_TYPE_NOT_SUPPORTED",
                "Media type not supported",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Message not readable: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "MESSAGE_NOT_READABLE",
                "Request body is not readable or malformed",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_PARAMETER",
                "Missing required parameter: " + ex.getParameterName(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "TYPE_MISMATCH",
                "Parameter type mismatch: " + ex.getName(),
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Generic exception handler

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper method to create standardized error responses

    private ErrorResponse createErrorResponse(int status, String code, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        String traceId = request.getHeader(TRACE_ID_HEADER);

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .error(HttpStatus.valueOf(status).getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .correlationId(correlationId)
                .traceId(traceId)
                .build();
    }
}