package com.gradlemedium200.dto;

/**
 * Represents the health status of an individual component in the system.
 * Used by the HealthStatus class to provide detailed health information.
 */
public class ComponentHealth {

    /**
     * Status of the component (UP, DOWN, OUT_OF_SERVICE)
     */
    private String status;
    
    /**
     * Detailed description or reason for the current status
     */
    private String details;
    
    /**
     * Optional error message if the component is in an error state
     */
    private String error;
    
    /**
     * Response time in milliseconds for the component
     */
    private Long responseTime;

    /**
     * Default constructor
     */
    public ComponentHealth() {
    }

    /**
     * Constructor with status
     *
     * @param status the component status
     */
    public ComponentHealth(String status) {
        this.status = status;
    }

    /**
     * Constructor with status and details
     *
     * @param status the component status
     * @param details the details of the status
     */
    public ComponentHealth(String status, String details) {
        this.status = status;
        this.details = details;
    }

    /**
     * Checks if the component is in a healthy state
     *
     * @return true if status is UP, false otherwise
     */
    public boolean isUp() {
        return "UP".equals(status);
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the details
     */
    public String getDetails() {
        return details;
    }

    /**
     * @param details the details to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return the error message
     */
    public String getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * @return the response time
     */
    public Long getResponseTime() {
        return responseTime;
    }

    /**
     * @param responseTime the response time to set
     */
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {
        return "ComponentHealth{" +
                "status='" + status + '\'' +
                ", details='" + details + '\'' +
                (error != null ? ", error='" + error + '\'' : "") +
                (responseTime != null ? ", responseTime=" + responseTime + "ms" : "") +
                '}';
    }
}