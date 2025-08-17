package com.gradlemedium200.common.exception;

import com.gradlemedium200.common.constants.ErrorCodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all custom exceptions in the application.
 * This class provides common functionality for handling errors with
 * error codes, context information, and timestamps.
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Error code associated with the exception
     */
    private final ErrorCodes errorCode;

    /**
     * Additional context information
     */
    private final Map<String, Object> context;

    /**
     * Exception occurrence timestamp
     */
    private final Long timestamp;

    /**
     * Constructor with error code and message
     *
     * @param errorCode The error code associated with this exception
     * @param message The error message
     */
    public BaseException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with error code, message, and cause
     *
     * @param errorCode The error code associated with this exception
     * @param message The error message
     * @param cause The original throwable cause
     */
    public BaseException(ErrorCodes errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the error code associated with this exception
     *
     * @return The error code
     */
    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    /**
     * Add context information to this exception
     *
     * @param key The context key
     * @param value The context value
     */
    public void addContext(String key, Object value) {
        if (key != null) {
            this.context.put(key, value);
        }
    }

    /**
     * Get the context information associated with this exception
     *
     * @return An unmodifiable view of the context map
     */
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    /**
     * Get the timestamp when this exception occurred
     *
     * @return The exception timestamp in milliseconds since epoch
     */
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName())
          .append(": ")
          .append(getMessage())
          .append(" [errorCode=")
          .append(errorCode)
          .append(", timestamp=")
          .append(timestamp)
          .append("]");
        
        if (!context.isEmpty()) {
            sb.append(" Context: ").append(context);
        }
        
        return sb.toString();
    }
}