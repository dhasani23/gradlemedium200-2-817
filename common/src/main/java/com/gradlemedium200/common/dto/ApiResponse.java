package com.gradlemedium200.common.dto;

import com.gradlemedium200.common.constants.ErrorCodes;
import java.io.Serializable;
import java.util.Objects;

/**
 * Standardized API response wrapper for all REST endpoints.
 * This class provides a consistent response format across all API operations,
 * including success status, messages, data payload, error codes, and timestamps.
 * 
 * @since 1.0
 */
public class ApiResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** Indicates if the operation was successful */
    private boolean success;
    
    /** Response message */
    private String message;
    
    /** Response data payload */
    private Object data;
    
    /** Error code if operation failed */
    private ErrorCodes errorCode;
    
    /** Response timestamp */
    private Long timestamp;
    
    /**
     * Default constructor.
     * Initializes a response with current timestamp.
     */
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Parameterized constructor.
     * 
     * @param success   Operation success status
     * @param message   Response message
     * @param data      Response data payload
     * @param errorCode Error code (if applicable)
     */
    public ApiResponse(boolean success, String message, Object data, ErrorCodes errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a successful response with data payload.
     * 
     * @param data The data to include in the response
     * @return A new ApiResponse instance indicating success
     */
    public static ApiResponse success(Object data) {
        return new ApiResponse(true, "Operation successful", data, null);
    }
    
    /**
     * Creates a successful response with custom message and data payload.
     * 
     * @param message The success message
     * @param data The data to include in the response
     * @return A new ApiResponse instance indicating success
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data, null);
    }
    
    /**
     * Creates an error response with error code and message.
     * 
     * @param errorCode The error code
     * @param message   Custom error message
     * @return A new ApiResponse instance indicating failure
     */
    public static ApiResponse error(ErrorCodes errorCode, String message) {
        return new ApiResponse(false, message, null, errorCode);
    }
    
    /**
     * Creates an error response with error code, using the default error message.
     * 
     * @param errorCode The error code
     * @return A new ApiResponse instance indicating failure
     */
    public static ApiResponse error(ErrorCodes errorCode) {
        return new ApiResponse(false, errorCode.getMessage(), null, errorCode);
    }
    
    /**
     * Checks if the response indicates a successful operation.
     * 
     * @return true if the operation was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get success status.
     * 
     * @return the success status
     */
    public boolean getSuccess() {
        return success;
    }
    
    /**
     * Set success status.
     * 
     * @param success the success status to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Get response message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Set response message.
     * 
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Get response data payload.
     * 
     * @return the data
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Set response data payload.
     * 
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * Get error code.
     * 
     * @return the errorCode
     */
    public ErrorCodes getErrorCode() {
        return errorCode;
    }
    
    /**
     * Set error code.
     * 
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(ErrorCodes errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Get response timestamp.
     * 
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set response timestamp.
     * 
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponse that = (ApiResponse) o;
        return success == that.success &&
                Objects.equals(message, that.message) &&
                Objects.equals(data, that.data) &&
                errorCode == that.errorCode &&
                Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(success, message, data, errorCode, timestamp);
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode=" + errorCode +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // TODO: Consider adding support for typed responses with generics
    
    // FIXME: Add proper serialization/deserialization of complex data objects
}