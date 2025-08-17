package com.gradlemedium200.common.constants;

/**
 * Application-wide constants for system configuration, status codes, and default values.
 * These constants are used throughout the application to maintain consistency
 * in configuration settings, formatting patterns, and default values.
 * 
 * @since 1.0
 */
public final class Constants {
    
    /**
     * Private constructor to prevent instantiation.
     * This class should only be used for its static constants.
     */
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    /**
     * Default page size for paginated responses.
     * Used when no specific page size is requested by the client.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Maximum allowed page size for paginated responses.
     * This helps prevent performance issues with excessively large page requests.
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Default timeout for operations in seconds.
     * Applied to various operations throughout the system when a specific
     * timeout is not provided.
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    /**
     * Standard date format pattern used across the application.
     * Format: yyyy-MM-dd (e.g., 2023-10-25)
     */
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    
    /**
     * Standard datetime format pattern used across the application.
     * Format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z' (ISO-8601 format with milliseconds)
     */
    public static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    /**
     * UTC timezone identifier.
     * Used for standardizing time zones across the application.
     */
    public static final String UTC_TIMEZONE = "UTC";
    
    // Common HTTP status codes
    /**
     * HTTP status code for success (200 OK)
     */
    public static final int HTTP_STATUS_OK = 200;
    
    /**
     * HTTP status code for created (201 Created)
     */
    public static final int HTTP_STATUS_CREATED = 201;
    
    /**
     * HTTP status code for bad request (400 Bad Request)
     */
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
    
    /**
     * HTTP status code for unauthorized (401 Unauthorized)
     */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;
    
    /**
     * HTTP status code for forbidden (403 Forbidden)
     */
    public static final int HTTP_STATUS_FORBIDDEN = 403;
    
    /**
     * HTTP status code for not found (404 Not Found)
     */
    public static final int HTTP_STATUS_NOT_FOUND = 404;
    
    /**
     * HTTP status code for internal server error (500 Internal Server Error)
     */
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
    
    // API endpoint path prefixes
    /**
     * Base API path prefix
     */
    public static final String API_BASE_PATH = "/api/v1";
    
    // FIXME: Consider making these configurable through properties file
    /**
     * Default character encoding for string operations
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    /**
     * Default locale for internationalization
     */
    public static final String DEFAULT_LOCALE = "en_US";
    
    // TODO: Add security-related constants like token expiration times
    
    /**
     * Maximum length of user passwords
     */
    public static final int MAX_PASSWORD_LENGTH = 128;
    
    /**
     * Minimum length of user passwords
     */
    public static final int MIN_PASSWORD_LENGTH = 8;
}