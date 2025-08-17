package com.gradlemedium200.common.constants;

/**
 * Enumeration of all error codes used throughout the application.
 * This enum provides standardized error codes and messages for consistent
 * error handling and reporting across all modules.
 * 
 * @since 1.0
 */
public enum ErrorCodes {
    
    /**
     * Error code for validation failures such as invalid input format,
     * missing required fields, or value constraint violations.
     */
    VALIDATION_ERROR("VAL-001", "Validation error occurred"),
    
    /**
     * Error code for when a requested resource cannot be found in the system.
     * For example, trying to access a user, product, or order that doesn't exist.
     */
    RESOURCE_NOT_FOUND("RES-001", "Resource not found"),
    
    /**
     * Error code for business logic violations such as insufficient inventory,
     * conflicting operations, or business rule violations.
     */
    BUSINESS_LOGIC_ERROR("BIZ-001", "Business logic error occurred"),
    
    /**
     * Error code for internal system errors such as database connection issues,
     * third-party integration failures, or unexpected exceptions.
     */
    SYSTEM_ERROR("SYS-001", "System error occurred"),
    
    /**
     * Error code for unauthorized access attempts, insufficient permissions,
     * expired sessions, or authentication failures.
     */
    UNAUTHORIZED_ACCESS("AUTH-001", "Unauthorized access");
    
    private final String code;
    private final String message;
    
    /**
     * Constructor for ErrorCodes enum.
     * 
     * @param code    The unique error code string identifier
     * @param message The default error message associated with this code
     */
    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * Gets the error code string.
     * 
     * @return The unique error code string identifier
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Gets the default error message.
     * 
     * @return The default error message associated with this error code
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Finds an ErrorCode by its code string.
     * 
     * @param code The code string to search for
     * @return The matching ErrorCodes enum value or null if not found
     */
    public static ErrorCodes getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ErrorCodes errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a formatted string representation of this error code.
     * 
     * @return A string with both code and message in format: [CODE] MESSAGE
     */
    @Override
    public String toString() {
        return "[" + code + "] " + message;
    }
    
    // TODO: Consider adding methods to create custom error messages with parameters
    
    // FIXME: We may need to introduce categorized error codes in the future for better organization
}