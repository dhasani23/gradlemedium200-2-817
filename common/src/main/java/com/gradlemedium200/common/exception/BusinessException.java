package com.gradlemedium200.common.exception;

import com.gradlemedium200.common.constants.ErrorCodes;

/**
 * Exception class for business logic violations.
 * This exception is thrown when a business rule or constraint is violated.
 * It provides information about the specific business rule that was violated
 * and the severity level of the violation.
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Identifier of the business rule that was violated
     */
    private final String businessRuleId;
    
    /**
     * Severity level of the business exception (e.g., "WARNING", "ERROR", "CRITICAL")
     */
    private String severity;

    /**
     * Constructor with business error message
     *
     * @param message The error message describing the business rule violation
     */
    public BusinessException(String message) {
        super(ErrorCodes.BUSINESS_LOGIC_ERROR, message);
        this.businessRuleId = null;
        this.severity = "ERROR"; // Default severity level
        
        // Add context information about this being a business exception
        addContext("exceptionType", "BusinessException");
    }

    /**
     * Constructor with business rule ID and message
     *
     * @param businessRuleId Identifier of the violated business rule
     * @param message The error message describing the business rule violation
     */
    public BusinessException(String businessRuleId, String message) {
        super(ErrorCodes.BUSINESS_LOGIC_ERROR, message);
        this.businessRuleId = businessRuleId;
        this.severity = "ERROR"; // Default severity level
        
        // Add business rule ID to context for better debugging and logging
        addContext("businessRuleId", businessRuleId);
        addContext("exceptionType", "BusinessException");
    }
    
    /**
     * Constructor with business rule ID, message, and custom error code
     * 
     * @param businessRuleId Identifier of the violated business rule
     * @param message The error message describing the business rule violation
     * @param errorCode Custom error code for this specific business exception
     */
    public BusinessException(String businessRuleId, String message, ErrorCodes errorCode) {
        super(errorCode, message);
        this.businessRuleId = businessRuleId;
        this.severity = "ERROR"; // Default severity level
        
        // Add business rule ID to context for better debugging and logging
        addContext("businessRuleId", businessRuleId);
        addContext("exceptionType", "BusinessException");
    }
    
    /**
     * Constructor with business rule ID, message, and severity
     * 
     * @param businessRuleId Identifier of the violated business rule
     * @param message The error message describing the business rule violation
     * @param severity The severity level of this business exception
     */
    public BusinessException(String businessRuleId, String message, String severity) {
        super(ErrorCodes.BUSINESS_LOGIC_ERROR, message);
        this.businessRuleId = businessRuleId;
        this.severity = severity;
        
        // Add business rule ID and severity to context for better debugging and logging
        addContext("businessRuleId", businessRuleId);
        addContext("severity", severity);
        addContext("exceptionType", "BusinessException");
    }

    /**
     * Get the business rule ID
     *
     * @return The identifier of the violated business rule
     */
    public String getBusinessRuleId() {
        return businessRuleId;
    }

    /**
     * Get the severity level
     *
     * @return The severity level of the business exception
     */
    public String getSeverity() {
        return severity;
    }
    
    /**
     * Set the severity level of the business exception
     * 
     * @param severity The new severity level
     */
    public void setSeverity(String severity) {
        this.severity = severity;
        addContext("severity", severity);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (businessRuleId != null) {
            sb.append(" [businessRuleId=").append(businessRuleId).append("]");
        }
        sb.append(" [severity=").append(severity).append("]");
        return sb.toString();
    }
    
    // TODO: Add method to classify business exceptions based on rule types
    
    // FIXME: Consider standardizing severity levels through an enum
}