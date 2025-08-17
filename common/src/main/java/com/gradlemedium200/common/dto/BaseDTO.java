package com.gradlemedium200.common.dto;

import java.io.Serializable;

/**
 * BaseDTO - Base data transfer object with common properties for all DTOs
 * 
 * This class serves as the foundation for all Data Transfer Objects in the application,
 * providing common fields that are required across different data entities.
 * 
 * @author gradlemedium200
 * @version 1.0
 */
public class BaseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the DTO
     */
    private String id;
    
    /**
     * Timestamp when the DTO was created
     */
    private Long timestamp;
    
    /**
     * Request identifier for tracking purposes
     */
    private String requestId;
    
    /**
     * Default constructor
     */
    public BaseDTO() {
        // Initialize timestamp to current time by default
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with id
     *
     * @param id the unique identifier
     */
    public BaseDTO(String id) {
        this();
        this.id = id;
    }
    
    /**
     * Constructor with all parameters
     *
     * @param id the unique identifier
     * @param timestamp the creation timestamp
     * @param requestId the request identifier
     */
    public BaseDTO(String id, Long timestamp, String requestId) {
        this.id = id;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    /**
     * Gets the unique identifier
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the timestamp
     *
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the request identifier
     * 
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Sets the request identifier
     * 
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    @Override
    public String toString() {
        return "BaseDTO [id=" + id + ", timestamp=" + timestamp + ", requestId=" + requestId + "]";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BaseDTO baseDTO = (BaseDTO) o;
        
        return id != null ? id.equals(baseDTO.id) : baseDTO.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}