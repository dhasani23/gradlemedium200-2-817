package com.gradlemedium200.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.gradlemedium200.common.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler for consistent error responses.
 * This class provides centralized exception handling for the application,
 * ensuring that all error responses follow a consistent format and logging.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Logger for exception handling
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Flag to include stack traces in responses
     */
    private boolean includeStackTrace = false;

    /**
     * Handles custom application exceptions
     *
     * @param ex The ApplicationException that was thrown
     * @return A ResponseEntity with a standardized error response
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        logger.warn("Application exception: {} (Error code: {})", 
                ex.getMessage(), ex.getErrorCode());
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus(),
                extractPath(),
                ex
        );
        
        // Add context information to error response if available
        if (!ex.getContext().isEmpty()) {
            errorResponse.getDetails().putAll(ex.getContext());
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatus()));
    }

    /**
     * Handles validation exceptions
     *
     * @param ex The ValidationException that was thrown
     * @return A ResponseEntity with a standardized error response
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        logger.warn("Validation exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                "VALIDATION_ERROR", // Using hardcoded value instead of ex.getErrorCode().getCode()
                HttpStatus.BAD_REQUEST.value(),
                extractPath(),
                ex
        );
        
        // Add field errors to the error response details
        if (!ex.getFieldErrors().isEmpty()) {
            errorResponse.addDetail("fieldErrors", ex.getFieldErrors());
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles service unavailable exceptions
     *
     * @param ex The ServiceUnavailableException that was thrown
     * @return A ResponseEntity with a standardized error response
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
        logger.error("Service unavailable: {} (Service: {})", ex.getMessage(), ex.getServiceName(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                "SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                extractPath(),
                ex
        );
        
        errorResponse.addDetail("service", ex.getServiceName());
        
        HttpHeaders headers = new HttpHeaders();
        
        // Add retry-after header if specified
        if (ex.getRetryAfterMs() != null) {
            long retryAfterSeconds = ex.getRetryAfterMs() / 1000;
            headers.set("Retry-After", String.valueOf(retryAfterSeconds));
            errorResponse.addDetail("retryAfterSeconds", retryAfterSeconds);
        }
        
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handles all other unexpected exceptions
     *
     * @param ex The Exception that was thrown
     * @return A ResponseEntity with a standardized error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Generate unique request ID for tracking this error
        String requestId = UUID.randomUUID().toString();
        
        logger.error("Unexpected error occurred (requestId: {}): ", requestId, ex);
        
        // Do not expose detailed technical information in the response
        ErrorResponse errorResponse = buildErrorResponse(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                extractPath(),
                ex
        );
        
        errorResponse.setRequestId(requestId);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Builds a standardized error response
     *
     * @param message The error message
     * @param errorCode The error code
     * @param status The HTTP status code
     * @param path The request path
     * @param ex The original exception
     * @return A standardized ErrorResponse object
     */
    private ErrorResponse buildErrorResponse(String message, String errorCode, int status, String path, Exception ex) {
        ErrorResponse.Builder builder = ErrorResponse.builder()
                .withMessage(message)
                .withError(errorCode)
                .withStatus(status)
                .withPath(path)
                .withTimestamp(LocalDateTime.now())
                .withRequestId(UUID.randomUUID().toString());
        
        // Include stack trace in response if enabled (useful for development)
        if (includeStackTrace && ex != null) {
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : ex.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            builder.withDetail("stackTrace", stackTrace.toString());
        }
        
        return builder.build();
    }
    
    /**
     * Extracts the request path from the current request context
     *
     * @return The request path or "unknown" if not available
     */
    private String extractPath() {
        try {
            ServletWebRequest webRequest = (ServletWebRequest) org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes();
            
            return webRequest.getRequest().getRequestURI();
        } catch (Exception e) {
            logger.debug("Could not extract request path", e);
        }
        
        // FIXME: Improve path extraction for non-servlet web requests
        return "unknown";
    }
    
    /**
     * Sets whether stack traces should be included in error responses
     *
     * @param includeStackTrace true to include stack traces, false otherwise
     */
    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
        logger.info("Stack trace inclusion in error responses set to: {}", includeStackTrace);
    }
    
    /**
     * Checks if stack traces are included in error responses
     *
     * @return true if stack traces are included, false otherwise
     */
    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }
    
    // TODO: Add specialized handlers for security exceptions (403, 401)
    // TODO: Add handling for constraint violation exceptions
}