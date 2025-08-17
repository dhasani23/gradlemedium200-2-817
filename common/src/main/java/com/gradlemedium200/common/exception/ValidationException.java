package com.gradlemedium200.common.exception;

import com.gradlemedium200.common.constants.ErrorCodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 * This class extends the BaseException and adds support for field-specific validation errors.
 */
public class ValidationException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * Map of field-specific validation errors where the key is the field name
     * and the value is the error message for that field.
     */
    private Map<String, String> fieldErrors;

    /**
     * Constructor with validation message.
     * 
     * @param message The validation error message
     */
    public ValidationException(String message) {
        super(ErrorCodes.VALIDATION_ERROR, message);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Constructor with field errors.
     * Creates an exception with field-specific validation errors.
     * 
     * @param fieldErrors Map of field-specific validation errors
     */
    public ValidationException(Map<String, String> fieldErrors) {
        super(ErrorCodes.VALIDATION_ERROR, "Validation failed for one or more fields");
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    /**
     * Get field-specific errors.
     * 
     * @return An unmodifiable view of the field errors map
     */
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    /**
     * Add a field error.
     * 
     * @param field The field name that failed validation
     * @param error The validation error message for the field
     */
    public void addFieldError(String field, String error) {
        if (field != null && error != null) {
            this.fieldErrors.put(field, error);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        
        if (!fieldErrors.isEmpty()) {
            sb.append(" Field errors: ").append(fieldErrors);
        }
        
        return sb.toString();
    }
}