package com.gradlemedium200.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown for validation errors.
 * This exception is used when validating user input, form data, or API request data
 * to report field-level validation failures.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the field that failed validation
     */
    private String fieldName;
    
    /**
     * Value that was rejected during validation
     */
    private Object rejectedValue;
    
    /**
     * List of validation error messages
     */
    private List<String> validationErrors;

    /**
     * Constructor with validation message.
     *
     * @param message The validation error message
     */
    public ValidationException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }

    /**
     * Constructor with field name and rejected value.
     *
     * @param fieldName Name of the field that failed validation
     * @param rejectedValue Value that was rejected during validation
     */
    public ValidationException(String fieldName, Object rejectedValue) {
        super("Validation failed for field '" + fieldName + "' with value: " + rejectedValue);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add("Invalid value for field: " + fieldName);
    }

    /**
     * Adds a validation error to the list.
     *
     * @param error The validation error message to add
     */
    public void addValidationError(String error) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(error);
    }

    /**
     * Gets the name of the field that failed validation.
     *
     * @return The field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the name of the field that failed validation.
     *
     * @param fieldName The field name to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the value that was rejected during validation.
     *
     * @return The rejected value
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * Sets the value that was rejected during validation.
     *
     * @param rejectedValue The rejected value to set
     */
    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    /**
     * Gets the list of validation error messages.
     *
     * @return The list of validation error messages
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Sets the list of validation error messages.
     *
     * @param validationErrors The list of validation error messages to set
     */
    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
    
    /**
     * Returns true if there are any validation errors.
     * 
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
    
    /**
     * Returns the number of validation errors.
     * 
     * @return the count of validation errors
     */
    public int getErrorCount() {
        return validationErrors == null ? 0 : validationErrors.size();
    }

    /**
     * Returns a string representation of all validation errors.
     * 
     * @return a combined string of all validation errors
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationException: ");
        
        if (fieldName != null) {
            sb.append("Field '").append(fieldName).append("'");
            if (rejectedValue != null) {
                sb.append(" with value '").append(rejectedValue).append("'");
            }
            sb.append(": ");
        }
        
        if (validationErrors != null && !validationErrors.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (String error : validationErrors) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(error);
                first = false;
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}