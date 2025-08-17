package com.gradlemedium200.common.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for validation results containing success status and error messages.
 * This class is used across the application to maintain consistent validation response format.
 * 
 * @since 1.0
 */
public class ValidationResult {

    /**
     * Flag indicating if the validation passed
     */
    private boolean valid;
    
    /**
     * List of general error messages
     */
    private List<String> errorMessages;
    
    /**
     * Map of field-specific error messages (fieldName -> errorMessage)
     */
    private Map<String, String> fieldErrors;
    
    /**
     * List of warning messages that don't invalidate the validation
     */
    private List<String> warningMessages;
    
    /**
     * Default constructor initializing a valid result with empty collections
     */
    public ValidationResult() {
        this.valid = true;
        this.errorMessages = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
        this.warningMessages = new ArrayList<>();
    }
    
    /**
     * Constructor that sets the initial validity state
     * 
     * @param valid initial validation state
     */
    public ValidationResult(boolean valid) {
        this();
        this.valid = valid;
    }
    
    /**
     * Checks if the validation is successful
     * 
     * @return true if validation passed, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Sets the validation status
     * 
     * @param valid the validation status to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /**
     * Adds an error message and marks validation as failed
     * 
     * @param message the error message to add
     */
    public void addError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            this.errorMessages.add(message);
            this.valid = false;
        }
    }
    
    /**
     * Adds a field-specific error message and marks validation as failed
     * 
     * @param field the field name that has an error
     * @param message the error message for the field
     */
    public void addFieldError(String field, String message) {
        if (field != null && message != null && !field.trim().isEmpty() && !message.trim().isEmpty()) {
            this.fieldErrors.put(field, message);
            this.valid = false;
        }
    }
    
    /**
     * Checks if there are any error messages or field errors
     * 
     * @return true if there are any errors, false otherwise
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty() || !fieldErrors.isEmpty();
    }
    
    /**
     * Adds a warning message (does not affect validation status)
     * 
     * @param message the warning message to add
     */
    public void addWarning(String message) {
        if (message != null && !message.trim().isEmpty()) {
            this.warningMessages.add(message);
        }
    }
    
    /**
     * Checks if there are any warning messages
     * 
     * @return true if there are any warnings, false otherwise
     */
    public boolean hasWarnings() {
        return !warningMessages.isEmpty();
    }

    /**
     * Get the list of error messages
     * 
     * @return list of error messages
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Get the map of field-specific error messages
     * 
     * @return map of field-specific errors
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * Get the list of warning messages
     * 
     * @return list of warning messages
     */
    public List<String> getWarningMessages() {
        return warningMessages;
    }
    
    /**
     * Merges another validation result into this one
     * 
     * @param other the other validation result to merge
     * @return this validation result instance for method chaining
     */
    public ValidationResult merge(ValidationResult other) {
        if (other == null) {
            return this;
        }
        
        // Merge validity status
        this.valid = this.valid && other.valid;
        
        // Merge error messages
        this.errorMessages.addAll(other.errorMessages);
        
        // Merge field errors
        this.fieldErrors.putAll(other.fieldErrors);
        
        // Merge warning messages
        this.warningMessages.addAll(other.warningMessages);
        
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult [valid=").append(valid).append(", ");
        
        if (!errorMessages.isEmpty()) {
            sb.append("errors=").append(errorMessages).append(", ");
        }
        
        if (!fieldErrors.isEmpty()) {
            sb.append("fieldErrors=").append(fieldErrors).append(", ");
        }
        
        if (!warningMessages.isEmpty()) {
            sb.append("warnings=").append(warningMessages);
        }
        
        sb.append("]");
        return sb.toString();
    }
}