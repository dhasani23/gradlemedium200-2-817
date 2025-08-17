package com.gradlemedium200.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for standardized error responses in API.
 * This class provides a consistent format for error responses across the application.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;
    private Map<String, Object> details;

    /**
     * Default constructor for ErrorResponse.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    /**
     * Parameterized constructor for ErrorResponse.
     *
     * @param status    HTTP status code
     * @param error     Error type or category
     * @param message   Detailed error message
     * @param path      Request path that caused the error
     * @param requestId Unique request identifier for tracing
     */
    public ErrorResponse(int status, String error, String message, String path, String requestId) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
        this.details = new HashMap<>();
    }

    /**
     * Get the timestamp when the error occurred.
     *
     * @return The error timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set the error timestamp.
     *
     * @param timestamp The error timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the HTTP status code.
     *
     * @return The HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the HTTP status code.
     *
     * @param status The HTTP status code
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Get the error type or category.
     *
     * @return The error type
     */
    public String getError() {
        return error;
    }

    /**
     * Set the error type or category.
     *
     * @param error The error type
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Get the detailed error message.
     *
     * @return The error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the detailed error message.
     *
     * @param message The error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the request path that caused the error.
     *
     * @return The request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the request path that caused the error.
     *
     * @param path The request path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the unique request identifier for tracing.
     *
     * @return The request identifier
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Set the unique request identifier for tracing.
     *
     * @param requestId The request identifier
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Get additional error details as a map.
     *
     * @return The error details map
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Set additional error details.
     *
     * @param details The error details map
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    /**
     * Add a detail entry to the error details map.
     *
     * @param key The detail key
     * @param value The detail value
     */
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    /**
     * Builder class for creating ErrorResponse instances with a fluent API.
     */
    public static class Builder {
        private final ErrorResponse errorResponse = new ErrorResponse();

        public Builder withStatus(int status) {
            errorResponse.setStatus(status);
            return this;
        }

        public Builder withError(String error) {
            errorResponse.setError(error);
            return this;
        }

        public Builder withMessage(String message) {
            errorResponse.setMessage(message);
            return this;
        }

        public Builder withPath(String path) {
            errorResponse.setPath(path);
            return this;
        }

        public Builder withRequestId(String requestId) {
            errorResponse.setRequestId(requestId);
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp) {
            errorResponse.setTimestamp(timestamp);
            return this;
        }
        
        public Builder withDetail(String key, Object value) {
            errorResponse.addDetail(key, value);
            return this;
        }

        public ErrorResponse build() {
            return errorResponse;
        }
    }

    /**
     * Creates a new builder instance for constructing ErrorResponse objects.
     *
     * @return A new ErrorResponse.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}