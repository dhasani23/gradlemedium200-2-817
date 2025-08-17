package com.gradlemedium200.common.constants;

/**
 * Business logic constants including status values, error codes, and configuration keys.
 * This class provides centralized constants used across the application for business operations,
 * including status indicators, user identifiers, and system configuration values.
 *
 * @since 1.0.0
 */
public final class ApplicationConstants {

    /**
     * Private constructor to prevent instantiation of constants class.
     */
    private ApplicationConstants() {
        throw new IllegalStateException("Constants class should not be instantiated");
    }

    /**
     * Success status constant used to indicate successful operations.
     * Used in business logic and API responses to indicate operation completed successfully.
     */
    public static final String SUCCESS_STATUS = "SUCCESS";
    
    /**
     * Error status constant used to indicate failed operations.
     * Used in business logic and API responses to indicate operation failed.
     */
    public static final String ERROR_STATUS = "ERROR";
    
    /**
     * Pending status constant used to indicate operations in progress.
     * Used for operations that have been initiated but not yet completed.
     */
    public static final String PENDING_STATUS = "PENDING";
    
    /**
     * System user identifier for automated operations.
     * Used to attribute system-generated changes in audit logs.
     */
    public static final String SYSTEM_USER = "SYSTEM";
    
    /**
     * Default currency code for monetary values.
     * Default is USD (US Dollar) unless overridden by system configuration.
     * 
     * TODO: Consider making this configurable via properties file
     */
    public static final String DEFAULT_CURRENCY = "USD";
    
    /**
     * Minimum password length requirement for user security.
     * Used in validation logic for password creation and updates.
     * 
     * FIXME: This should be aligned with security best practices and potentially
     * updated to require more complex password requirements.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    // Business domain groups - organized by functional area
    
    /* Order Processing Constants */
    public static final String ORDER_CREATED_STATUS = "CREATED";
    public static final String ORDER_PROCESSING_STATUS = "PROCESSING";
    public static final String ORDER_SHIPPED_STATUS = "SHIPPED";
    public static final String ORDER_DELIVERED_STATUS = "DELIVERED";
    public static final String ORDER_CANCELLED_STATUS = "CANCELLED";
    
    /* Payment Processing Constants */
    public static final String PAYMENT_PENDING_STATUS = "PENDING";
    public static final String PAYMENT_COMPLETED_STATUS = "COMPLETED";
    public static final String PAYMENT_FAILED_STATUS = "FAILED";
    public static final String PAYMENT_REFUNDED_STATUS = "REFUNDED";
    
    /* Configuration Keys */
    public static final String CONFIG_EMAIL_ENABLED = "email.notifications.enabled";
    public static final String CONFIG_SMS_ENABLED = "sms.notifications.enabled";
    public static final String CONFIG_RETRY_COUNT = "process.retry.count";
    
    /**
     * Maximum number of retry attempts for failed operations.
     * Used in retry logic for transient failures in external system interactions.
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Default page size for paginated results.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
}