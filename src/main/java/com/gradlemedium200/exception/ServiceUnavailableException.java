package com.gradlemedium200.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when a service is temporarily unavailable.
 * 
 * This exception indicates that a requested service is currently unavailable
 * and provides information about which service failed and when to retry.
 * It is used for both internal service communication failures and external service dependencies.
 */
public class ServiceUnavailableException extends ApplicationException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the unavailable service
     */
    private final String serviceName;
    
    /**
     * Suggested retry delay in milliseconds
     */
    private Long retryAfterMs;
    
    /**
     * Severity of the exception
     */
    private String severity;
    
    /**
     * Timestamp when the exception occurred
     */
    private LocalDateTime timestamp;

    /**
     * Error code for service unavailable exceptions
     */
    private static final String SERVICE_UNAVAILABLE_ERROR_CODE = "SVC-UNAVAIL";
    
    /**
     * Constructor with service name.
     *
     * @param serviceName Name of the unavailable service
     */
    public ServiceUnavailableException(String serviceName) {
        super(String.format("Service '%s' is temporarily unavailable", serviceName), SERVICE_UNAVAILABLE_ERROR_CODE);
        this.serviceName = serviceName;
        this.retryAfterMs = 0L; // Default retry delay is 0 (retry immediately)
        this.severity = "WARNING";
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with service name and retry delay.
     *
     * @param serviceName Name of the unavailable service
     * @param retryAfter Suggested retry delay in milliseconds
     */
    public ServiceUnavailableException(String serviceName, long retryAfter) {
        super(String.format("Service '%s' is temporarily unavailable. Retry after %d ms", serviceName, retryAfter), 
              SERVICE_UNAVAILABLE_ERROR_CODE);
        this.serviceName = serviceName;
        this.retryAfterMs = retryAfter;
        this.severity = "WARNING";
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Gets the name of the unavailable service.
     *
     * @return The service name
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Gets the suggested retry delay in milliseconds.
     *
     * @return The retry delay in milliseconds
     */
    public Long getRetryAfterMs() {
        return retryAfterMs;
    }
    
    /**
     * Sets the suggested retry delay in milliseconds.
     *
     * @param retryAfterMs The retry delay in milliseconds
     */
    public void setRetryAfterMs(Long retryAfterMs) {
        this.retryAfterMs = retryAfterMs;
    }
    
    /**
     * Gets the severity of the exception.
     *
     * @return The severity
     */
    public String getSeverity() {
        return severity;
    }
    
    /**
     * Sets the severity of the exception.
     *
     * @param severity The severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    /**
     * Gets the timestamp when the exception occurred.
     *
     * @return The timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp when the exception occurred.
     *
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Returns a string representation of this exception including the service name and retry delay
     *
     * @return String representation of the exception
     */
    @Override
    public String toString() {
        return String.format("ServiceUnavailableException[serviceName=%s, retryAfter=%d, errorCode=%s, severity=%s, timestamp=%s]", 
                serviceName, retryAfterMs, getErrorCode(), getSeverity(), getTimestamp());
    }
    
    // TODO: Add integration with circuit breaker pattern to prevent cascading failures
    
    // FIXME: Implement dynamic retry calculation based on service response times
}