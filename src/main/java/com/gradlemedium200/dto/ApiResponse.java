package com.gradlemedium200.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generic API response wrapper for consistent response format.
 * This class provides a standardized structure for all API responses
 * across the application, including success/error status, messages,
 * payload data, timestamps, and request identifiers for tracing.
 *
 * @author gradlemedium200
 */
public class ApiResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;
    private String requestId;
    
    /**
     * Default constructor initializes a response with current timestamp
     * and a generated request ID.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.requestId = UUID.randomUUID().toString();
    }
    
    /**
     * Creates a response with success status, message and data.
     *
     * @param success    whether the request was successful
     * @param message    response message
     * @param data       response data payload
     */
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.requestId = UUID.randomUUID().toString();
    }
    
    /**
     * Parameterized constructor for creating a response with all fields.
     *
     * @param success    whether the request was successful
     * @param message    response message
     * @param data       response data payload
     * @param timestamp  response timestamp
     * @param requestId  unique request identifier
     */
    public ApiResponse(boolean success, String message, Object data, LocalDateTime timestamp, String requestId) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.requestId = requestId != null ? requestId : UUID.randomUUID().toString();
    }
    
    /**
     * Creates a successful response with data payload.
     *
     * @param data  response data payload
     * @return      a new ApiResponse instance with success status
     */
    public static ApiResponse success(Object data) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Request processed successfully");
        response.setData(data);
        return response;
    }
    
    /**
     * Creates a successful response with custom message and data payload.
     *
     * @param message  custom success message
     * @param data     response data payload
     * @return         a new ApiResponse instance with success status
     */
    public static ApiResponse success(String message, Object data) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * Creates a successful response with paginated data.
     *
     * @param data       response data payload
     * @param totalItems total number of items available
     * @param page       current page number
     * @param totalPages total number of pages
     * @return           a new ApiResponse instance with success status and pagination info
     */
    public static ApiResponse success(Object data, Integer totalItems, int page, Integer totalPages) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Request processed successfully");
        response.setData(data);
        
        // Create a wrapper that includes pagination info
        response.setData(new Object() {
            public final Object items = data;
            public final Integer totalItemsCount = totalItems;
            public final int pageNumber = page;
            public final Integer totalPagesCount = totalPages;
        });
        
        return response;
    }
    
    /**
     * Creates an error response with an error message.
     *
     * @param message  error message describing what went wrong
     * @return         a new ApiResponse instance with error status
     */
    public static ApiResponse error(String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
    
    /**
     * Checks if the response indicates success.
     *
     * @return true if the response represents a successful operation
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Sets the success status of the response.
     *
     * @param success  whether the request was successful
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Gets the response message.
     *
     * @return the message describing the response result
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the response message.
     *
     * @param message  the message describing the response result
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the response data payload.
     *
     * @return the data payload of the response
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Sets the response data payload.
     *
     * @param data  the data payload of the response
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * Gets the response timestamp.
     *
     * @return the timestamp when the response was created
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the response timestamp.
     *
     * @param timestamp  the timestamp when the response was created
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the unique request identifier.
     *
     * @return the unique identifier for request tracing
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Sets the unique request identifier.
     *
     * @param requestId  the unique identifier for request tracing
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", requestId='" + requestId + '\'' +
                '}';
    }
    
    // TODO: Add type-safe methods for handling specific response data types
    
    // FIXME: Consider adding JSON serialization annotations for consistent formatting
}