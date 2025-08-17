package com.gradlemedium200.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> fieldErrors;
    
    /**
     * Constructor with message.
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Constructor with message and cause.
     * @param message the error message
     * @param cause the cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Constructor with field errors.
     * @param fieldErrors the field errors
     */
    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed for one or more fields");
        this.fieldErrors = new HashMap<>(fieldErrors);
    }
    
    /**
     * Constructor with message and field errors.
     * @param message the error message
     * @param fieldErrors the field errors
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = new HashMap<>(fieldErrors);
    }
    
    /**
     * Get the field errors.
     * @return the field errors
     */
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
    
    /**
     * Add a field error.
     * @param field the field name
     * @param message the error message
     */
    public void addFieldError(String field, String message) {
        fieldErrors.put(field, message);
    }
}