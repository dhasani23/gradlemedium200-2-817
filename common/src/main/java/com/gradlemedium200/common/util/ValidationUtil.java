package com.gradlemedium200.common.util;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.gradlemedium200.common.dto.ValidationResult;

/**
 * Utility class for common validation operations used across the application.
 * Provides methods for validating email addresses, phone numbers, required fields,
 * string lengths, and numeric ranges.
 *
 * @since 1.0
 */
public final class ValidationUtil {

    /**
     * Regular expression pattern for validating email addresses.
     * Follows standard RFC 5322 format with some practical limitations.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    /**
     * Regular expression pattern for validating phone numbers.
     * Supports formats: XXX-XXX-XXXX, (XXX) XXX-XXXX, XXXXXXXXXX, XXX.XXX.XXXX
     */
    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
    );
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Validates whether a given string is a valid email address format.
     *
     * @param email the email address to validate
     * @return true if the email is in a valid format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
    
    /**
     * Validates whether a given string is a valid phone number format.
     *
     * @param phoneNumber the phone number to validate
     * @return true if the phone number is in a valid format, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = PHONE_PATTERN.matcher(phoneNumber);
        return matcher.matches();
    }
    
    /**
     * Validates that a required value is not null or empty.
     *
     * @param value the value to validate
     * @param fieldName the name of the field being validated (for error message)
     * @return a ValidationResult indicating success or failure with appropriate message
     */
    public static ValidationResult validateRequired(Object value, String fieldName) {
        ValidationResult result = new ValidationResult();
        
        if (value == null) {
            result.addFieldError(fieldName, fieldName + " is required");
            return result;
        }
        
        // Check for empty strings
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            result.addFieldError(fieldName, fieldName + " is required");
            return result;
        }
        
        // Check for empty collections
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
            result.addFieldError(fieldName, fieldName + " cannot be empty");
            return result;
        }
        
        return result;
    }
    
    /**
     * Validates that a string's length is within the specified range.
     *
     * @param value the string to validate
     * @param minLength the minimum allowed length
     * @param maxLength the maximum allowed length
     * @param fieldName the name of the field being validated (for error message)
     * @return a ValidationResult indicating success or failure with appropriate message
     */
    public static ValidationResult validateLength(String value, int minLength, int maxLength, String fieldName) {
        ValidationResult result = new ValidationResult();
        
        if (value == null) {
            // Let validateRequired handle null check if needed
            return result;
        }
        
        int length = value.length();
        
        if (length < minLength) {
            result.addFieldError(fieldName, fieldName + " must be at least " + minLength + " characters long");
        }
        
        if (length > maxLength) {
            result.addFieldError(fieldName, fieldName + " cannot exceed " + maxLength + " characters");
        }
        
        return result;
    }
    
    /**
     * Validates that a numeric value is within the specified range.
     *
     * @param value the numeric value to validate
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param fieldName the name of the field being validated (for error message)
     * @return a ValidationResult indicating success or failure with appropriate message
     */
    public static ValidationResult validateRange(Number value, Number min, Number max, String fieldName) {
        ValidationResult result = new ValidationResult();
        
        if (value == null) {
            // Let validateRequired handle null check if needed
            return result;
        }
        
        double doubleValue = value.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        
        if (doubleValue < minValue) {
            result.addFieldError(fieldName, fieldName + " must be at least " + min);
        }
        
        if (doubleValue > maxValue) {
            result.addFieldError(fieldName, fieldName + " cannot exceed " + max);
        }
        
        return result;
    }
    
    /**
     * Convenience method to validate multiple conditions and combine the results.
     * 
     * @param results array of ValidationResult objects to combine
     * @return a combined ValidationResult containing all errors and warnings
     */
    public static ValidationResult combineResults(ValidationResult... results) {
        ValidationResult combined = new ValidationResult();
        
        if (results == null || results.length == 0) {
            return combined;
        }
        
        for (ValidationResult result : results) {
            if (result != null) {
                combined.merge(result);
            }
        }
        
        return combined;
    }
    
    /**
     * Validates if a string is a valid email format and generates a ValidationResult.
     * 
     * @param email the email to validate
     * @param fieldName the field name for the error message
     * @return a ValidationResult with appropriate error if invalid
     */
    public static ValidationResult validateEmail(String email, String fieldName) {
        ValidationResult result = new ValidationResult();
        
        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            result.addFieldError(fieldName, "Invalid email format");
        }
        
        return result;
    }
    
    /**
     * Validates if a string is a valid phone number format and generates a ValidationResult.
     * 
     * @param phoneNumber the phone number to validate
     * @param fieldName the field name for the error message
     * @return a ValidationResult with appropriate error if invalid
     */
    public static ValidationResult validatePhoneNumber(String phoneNumber, String fieldName) {
        ValidationResult result = new ValidationResult();
        
        if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !isValidPhoneNumber(phoneNumber)) {
            result.addFieldError(fieldName, "Invalid phone number format");
        }
        
        return result;
    }
}