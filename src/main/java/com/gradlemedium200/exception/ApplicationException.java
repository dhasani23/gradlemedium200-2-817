package com.gradlemedium200.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for application-specific exceptions.
 */
public class ApplicationException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    private final Map<String, Object> context;
    
    /**
     * Constructor with error message and error code.
     * @param message the error message
     * @param errorCode the error code
     */
    public ApplicationException(String message, String errorCode) {
        this(message, errorCode, 500);
    }
    
    /**
     * Constructor with error message, error code, and HTTP status code.
     * @param message the error message
     * @param errorCode the error code
     * @param httpStatus the HTTP status code
     */
    public ApplicationException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = new HashMap<>();
    }
    
    /**
     * Constructor with error message, error code, cause, and HTTP status code.
     * @param message the error message
     * @param errorCode the error code
     * @param cause the cause
     * @param httpStatus the HTTP status code
     */
    public ApplicationException(String message, String errorCode, Throwable cause, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = new HashMap<>();
    }
    
    /**
     * Get the error code.
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get the HTTP status code.
     * @return the HTTP status code
     */
    public int getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * Get the context.
     * @return the context
     */
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * Add context information.
     * @param key the context key
     * @param value the context value
     * @return this exception for method chaining
     */
    public ApplicationException addContext(String key, Object value) {
        context.put(key, value);
        return this;
    }
}