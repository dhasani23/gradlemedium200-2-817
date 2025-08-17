package com.gradlemedium200.productcatalog.exception;

import com.gradlemedium200.productcatalog.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API error responses and centralized error handling.
 * This class provides consistent error response formatting and logging across the application.
 * It catches exceptions thrown by controllers and converts them into standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger for exception handling
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Default constructor
     */
    public GlobalExceptionHandler() {
        logger.debug("GlobalExceptionHandler initialized");
    }

    /**
     * Handles ProductNotFoundException and returns 404 response
     *
     * @param ex The caught ProductNotFoundException
     * @param request The web request
     * @return ResponseEntity containing error details with 404 status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, 
            WebRequest request) {
        
        logger.warn("Product not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                "PRODUCT_NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles CategoryNotFoundException and returns 404 response
     *
     * @param ex The caught CategoryNotFoundException
     * @param request The web request
     * @return ResponseEntity containing error details with 404 status
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(
            CategoryNotFoundException ex,
            WebRequest request) {
        
        logger.warn("Category not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                "CATEGORY_NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InsufficientInventoryException and returns 409 response
     *
     * @param ex The caught InsufficientInventoryException
     * @param request The web request
     * @return ResponseEntity containing error details with 409 status
     */
    @ExceptionHandler(InsufficientInventoryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleInsufficientInventoryException(
            InsufficientInventoryException ex,
            WebRequest request) {
        
        logger.warn("Insufficient inventory: {} (Product ID: {}, Requested: {}, Available: {})", 
                ex.getMessage(), ex.getProductId(), ex.getRequestedQuantity(), ex.getAvailableQuantity());
        
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                "INSUFFICIENT_INVENTORY",
                HttpStatus.CONFLICT.value(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles validation errors and returns 400 response
     *
     * @param ex The caught MethodArgumentNotValidException
     * @param request The web request
     * @return ResponseEntity containing validation error details with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        // Collect all validation errors into a single string
        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        logger.warn("Validation error: {}", errorMsg);
        
        ErrorResponse errorResponse = buildErrorResponse(
                errorMsg,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic exceptions and returns 500 response
     *
     * @param ex The caught Exception
     * @param request The web request
     * @return ResponseEntity containing error details with 500 status
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        // For unexpected exceptions, log the full stack trace
        String requestId = UUID.randomUUID().toString();
        logger.error("Unexpected error occurred (requestId: {}): ", requestId, ex);
        
        // Do not expose technical details in the response
        ErrorResponse errorResponse = buildErrorResponse(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getPath(request)
        );
        
        // Set the requestId for tracking
        errorResponse.setRequestId(requestId);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Builds standardized error response
     *
     * @param message Error message
     * @param errorCode Error code/type
     * @param status HTTP status code
     * @param path Request path
     * @return Formatted ErrorResponse object
     */
    private ErrorResponse buildErrorResponse(String message, String errorCode, int status, String path) {
        return ErrorResponse.builder()
                .withMessage(message)
                .withError(errorCode)
                .withStatus(status)
                .withPath(path)
                .withTimestamp(LocalDateTime.now())
                .withRequestId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Extract the request path from WebRequest
     *
     * @param request The web request
     * @return The request path or "unknown" if not available
     */
    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        
        // FIXME: Improve path extraction for non-servlet web requests
        return "unknown";
    }

    // TODO: Add specialized handlers for security exceptions (403, 401)
    
    // TODO: Consider adding handlers for database-specific exceptions
}