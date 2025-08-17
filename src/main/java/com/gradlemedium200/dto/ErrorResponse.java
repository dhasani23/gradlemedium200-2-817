package com.gradlemedium200.dto;

import java.time.LocalDateTime;

/**
 * Error response model for API error responses.
 * Used to provide consistent error information across the application.
 */
public class ErrorResponse {
    private String error;           // Error type or category
    private String message;         // Human-readable error message
    private String details;         // Detailed error information
    private LocalDateTime timestamp; // Error occurrence timestamp
    private String path;            // Request path where error occurred
    private String requestId;       // Unique request identifier

    /**
     * Constructor with error type and message
     *
     * @param error   the error type or category
     * @param message the human-readable error message
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with error type, message and path
     *
     * @param error   the error type or category
     * @param message the human-readable error message
     * @param path    the request path where error occurred
     */
    public ErrorResponse(String error, String message, String path) {
        this(error, message);
        this.path = path;
    }

    /**
     * Default constructor for serialization/deserialization
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Get the error type
     *
     * @return the error type
     */
    public String getError() {
        return error;
    }

    /**
     * Set the error type
     *
     * @param error the error type to set
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Get the error message
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the error message
     *
     * @param message the error message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get detailed error information
     *
     * @return the detailed error information
     */
    public String getDetails() {
        return details;
    }

    /**
     * Set detailed error information
     *
     * @param details the detailed error information to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Get the error occurrence timestamp
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set the error occurrence timestamp
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the request path
     *
     * @return the request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the request path
     *
     * @param path the request path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the unique request identifier
     *
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Set the unique request identifier
     *
     * @param requestId the request ID to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Builder method to set detailed error information
     *
     * @param details the detailed error information
     * @return this ErrorResponse instance for method chaining
     */
    public ErrorResponse withDetails(String details) {
        this.details = details;
        return this;
    }

    /**
     * Builder method to set request ID
     *
     * @param requestId the request ID
     * @return this ErrorResponse instance for method chaining
     */
    public ErrorResponse withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }

    // TODO: Consider adding validation for required fields
    // FIXME: Timestamp serialization might need custom formatter for consistent output format
}