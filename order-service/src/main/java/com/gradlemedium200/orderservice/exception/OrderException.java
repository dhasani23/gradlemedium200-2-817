package com.gradlemedium200.orderservice.exception;

/**
 * Custom exception for order-related errors in the order service.
 * This exception is thrown when there are issues with order processing,
 * validation, or any other order-related operations.
 * 
 * The exception includes an error code that can be used to categorize
 * different types of order-related errors.
 */
public class OrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Error code associated with this exception to identify specific error scenarios
     */
    private String errorCode;
    
    /**
     * Creates a new OrderException with the specified error message.
     * The error code will be null in this case.
     *
     * @param message The error message describing the exception
     */
    public OrderException(String message) {
        super(message);
        // FIXME: Consider setting a default error code for better error handling
    }
    
    /**
     * Creates a new OrderException with the specified error message and error code.
     *
     * @param message The error message describing the exception
     * @param errorCode The error code identifying the specific error scenario
     */
    public OrderException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the error code associated with this exception.
     * Error codes can be used to identify specific error scenarios
     * and provide appropriate handling in the GlobalExceptionHandler.
     *
     * @return The error code, or null if no error code was provided
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    // TODO: Add constructor that accepts a cause (Throwable) for exception chaining
    // TODO: Add method to create standard order exceptions with predefined error codes
}