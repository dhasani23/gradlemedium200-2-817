package com.gradlemedium200.orderservice.exception;

/**
 * Custom exception for cart-related errors.
 * This exception is used to represent errors that occur during cart operations.
 */
public class CartException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Error code associated with this exception
     */
    private String errorCode;
    
    /**
     * Constructs a new CartException with the specified error message.
     * 
     * @param message the error message
     */
    public CartException(String message) {
        super(message);
        // Default error code when not specified
        this.errorCode = "CART_ERROR";
    }
    
    /**
     * Constructs a new CartException with the specified error message and error code.
     * 
     * @param message the error message
     * @param errorCode the error code
     */
    public CartException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the error code associated with this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Creates a formatted string representation of the exception.
     * 
     * @return a string containing both the message and error code
     */
    @Override
    public String toString() {
        return "CartException [errorCode=" + errorCode + ", message=" + getMessage() + "]";
    }
    
    // TODO: Add additional constructors for cause and suppression if needed
    
    /**
     * Checks if this exception matches a specific error code
     * 
     * @param code the error code to check against
     * @return true if the exception has the specified error code
     */
    public boolean hasErrorCode(String code) {
        return errorCode != null && errorCode.equals(code);
    }
}