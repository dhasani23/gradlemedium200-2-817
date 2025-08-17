package com.gradlemedium200.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for representing the health status of a service or component.
 */
public class HealthStatus {
    
    /**
     * Status enum representing different health states.
     */
    public enum Status {
        UP,
        DOWN,
        DEGRADED,
        UNKNOWN
    }
    
    /**
     * Class representing the health of a component.
     */
    public static class ComponentHealth {
        private Status status;
        private String details;
        
        /**
         * Constructor.
         * @param status the status of the component
         * @param details details about the component's health
         */
        public ComponentHealth(Status status, String details) {
            this.status = status;
            this.details = details;
        }
        
        /**
         * Get the status of the component.
         * @return the status
         */
        public Status getStatus() {
            return status;
        }
        
        /**
         * Set the status of the component.
         * @param status the status to set
         */
        public void setStatus(Status status) {
            this.status = status;
        }
        
        /**
         * Get the details about the component's health.
         * @return the details
         */
        public String getDetails() {
            return details;
        }
        
        /**
         * Set the details about the component's health.
         * @param details the details to set
         */
        public void setDetails(String details) {
            this.details = details;
        }
    }
    
    private Status overallStatus;
    private Map<String, ComponentHealth> components = new HashMap<>();
    private LocalDateTime timestamp;
    private String version;
    
    /**
     * Constructor with overall status.
     * @param overallStatus the overall status
     */
    public HealthStatus(Status overallStatus) {
        this.overallStatus = overallStatus;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Default constructor with UNKNOWN status.
     */
    public HealthStatus() {
        this(Status.UNKNOWN);
    }
    
    /**
     * Get the overall status.
     * @return the overall status
     */
    public Status getOverallStatus() {
        return overallStatus;
    }
    
    /**
     * Set the overall status.
     * @param overallStatus the overall status to set
     */
    public void setOverallStatus(Status overallStatus) {
        this.overallStatus = overallStatus;
    }
    
    /**
     * Get the timestamp when this health status was created.
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp when this health status was created.
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the version of the service.
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Set the version of the service.
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Get the components and their health statuses.
     * @return the components map
     */
    public Map<String, ComponentHealth> getComponents() {
        return components;
    }
    
    /**
     * Add a component with its health status.
     * @param name the component name
     * @param status the component status
     * @param details details about the component's health
     */
    public void addComponent(String name, Status status, String details) {
        components.put(name, new ComponentHealth(status, details));
    }
    
    /**
     * Update the overall status based on component statuses.
     * Overall status is DOWN if any component is DOWN, DEGRADED if any component is DEGRADED,
     * UP if all components are UP, and UNKNOWN if there are no components.
     */
    public void updateOverallStatus() {
        if (components.isEmpty()) {
            overallStatus = Status.UNKNOWN;
            return;
        }
        
        boolean hasDown = false;
        boolean hasDegraded = false;
        boolean hasUp = false;
        
        for (ComponentHealth health : components.values()) {
            switch (health.getStatus()) {
                case DOWN:
                    hasDown = true;
                    break;
                case DEGRADED:
                    hasDegraded = true;
                    break;
                case UP:
                    hasUp = true;
                    break;
                default:
                    // No action needed for UNKNOWN
                    break;
            }
        }
        
        if (hasDown) {
            overallStatus = Status.DOWN;
        } else if (hasDegraded) {
            overallStatus = Status.DEGRADED;
        } else if (hasUp) {
            overallStatus = Status.UP;
        } else {
            overallStatus = Status.UNKNOWN;
        }
    }
}