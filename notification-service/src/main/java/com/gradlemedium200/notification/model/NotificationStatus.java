package com.gradlemedium200.notification.model;

/**
 * Enumeration defining possible notification delivery statuses.
 * This enum is used throughout the notification service to track
 * the current state of a notification in its delivery lifecycle.
 * 
 * The status flow typically follows: PENDING -> SENT -> DELIVERED or FAILED
 * In case of failure, notifications may enter RETRY state.
 */
public enum NotificationStatus {
    
    /**
     * Notification is pending to be sent.
     * Initial state when a notification is created but not yet processed.
     */
    PENDING,
    
    /**
     * Notification has been sent to the delivery service.
     * The notification service has successfully handed off the notification
     * to the appropriate delivery channel (SNS, email service, etc.).
     */
    SENT,
    
    /**
     * Notification has been successfully delivered to the recipient.
     * Final successful state indicating the notification reached its destination.
     */
    DELIVERED,
    
    /**
     * Notification delivery failed.
     * Terminal error state when delivery has permanently failed after all retries.
     * 
     * FIXME: Consider adding failure reason codes to provide more detailed diagnostics.
     */
    FAILED,
    
    /**
     * Notification is queued for retry.
     * Intermediate state when delivery failed but will be attempted again.
     * 
     * TODO: Implement retry count and backoff mechanism to prevent infinite retry loops.
     */
    RETRY,
    
    /**
     * Notification is invalid.
     * State indicating the notification data is incomplete or invalid.
     */
    INVALID
}