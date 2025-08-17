package com.gradlemedium200.orderservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API error handling.
 * This class provides centralized exception handling for all controllers
 * in the order-service module, ensuring consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Error response class to standardize the error response structure
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private String message;
        private String errorCode;
        private Map<String, Object> details;
        
        public ErrorResponse() {
            this.timestamp = LocalDateTime.now();
            this.details = new HashMap<>();
        }
        
        public ErrorResponse(String message) {
            this();
            this.message = message;
        }
        
        public ErrorResponse(String message, String errorCode) {
            this(message);
            this.errorCode = errorCode;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
        
        public Map<String, Object> getDetails() {
            return details;
        }
        
        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
        
        public void addDetail(String key, Object value) {
            this.details.put(key, value);
        }
    }
    
    /**
     * Handles order-related exceptions.
     * 
     * @param ex The OrderException that was thrown
     * @param request The web request where the exception occurred
     * @return ResponseEntity with appropriate error details and status code
     */
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException ex, WebRequest request) {
        logger.error("Order exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getErrorCode());
        errorResponse.addDetail("requestUri", request.getDescription(false));
        errorResponse.addDetail("exceptionType", "OrderException");
        
        // Determine HTTP status based on error code or default to BAD_REQUEST
        HttpStatus status = determineStatusFromOrderErrorCode(ex.getErrorCode());
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles cart-related exceptions.
     * 
     * @param ex The CartException that was thrown
     * @param request The web request where the exception occurred
     * @return ResponseEntity with appropriate error details and status code
     */
    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex, WebRequest request) {
        logger.error("Cart exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getErrorCode());
        errorResponse.addDetail("requestUri", request.getDescription(false));
        errorResponse.addDetail("exceptionType", "CartException");
        
        // Most cart exceptions are likely client errors
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles payment-related exceptions.
     * 
     * @param ex The PaymentException that was thrown
     * @param request The web request where the exception occurred
     * @return ResponseEntity with appropriate error details and status code
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex, WebRequest request) {
        logger.error("Payment exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getErrorCode());
        errorResponse.addDetail("requestUri", request.getDescription(false));
        errorResponse.addDetail("exceptionType", "PaymentException");
        
        if (ex.getPaymentId() != null) {
            errorResponse.addDetail("paymentId", ex.getPaymentId());
        }
        
        // Determine HTTP status based on error code
        HttpStatus status = determineStatusFromPaymentErrorCode(ex.getErrorCode());
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles all other uncaught exceptions.
     * 
     * @param ex The exception that was thrown
     * @param request The web request where the exception occurred
     * @return ResponseEntity with appropriate error details and status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // For security, don't include stack traces or detailed internal errors in response
        logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred. Please try again later.", "INTERNAL_ERROR");
        errorResponse.addDetail("requestUri", request.getDescription(false));
        errorResponse.addDetail("exceptionType", ex.getClass().getSimpleName());
        
        // FIXME: In production, we should have more granular exception handling
        // for different types of exceptions (validation, auth, etc.)
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Maps order error codes to appropriate HTTP status codes.
     * 
     * @param errorCode The error code from the OrderException
     * @return The appropriate HTTP status code
     */
    private HttpStatus determineStatusFromOrderErrorCode(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        
        // Map different error codes to appropriate status codes
        switch (errorCode) {
            case "ORDER_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "ORDER_INVALID_STATE":
            case "ORDER_VALIDATION_FAILED":
                return HttpStatus.BAD_REQUEST;
            case "ORDER_UNAUTHORIZED":
                return HttpStatus.FORBIDDEN;
            case "ORDER_INVENTORY_INSUFFICIENT":
                return HttpStatus.CONFLICT;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
    
    /**
     * Maps payment error codes to appropriate HTTP status codes.
     * 
     * @param errorCode The error code from the PaymentException
     * @return The appropriate HTTP status code
     */
    private HttpStatus determineStatusFromPaymentErrorCode(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        
        // Map different error codes to appropriate status codes
        switch (errorCode) {
            case "PAYMENT_FAILED":
            case "PAYMENT_DECLINED":
                return HttpStatus.PAYMENT_REQUIRED;
            case "PAYMENT_GATEWAY_ERROR":
                return HttpStatus.SERVICE_UNAVAILABLE;
            case "PAYMENT_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "PAYMENT_INVALID_DATA":
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
    
    // TODO: Add more specialized exception handlers for validation errors, authentication errors, etc.
    // TODO: Consider implementing request/response logging for debug purposes
}