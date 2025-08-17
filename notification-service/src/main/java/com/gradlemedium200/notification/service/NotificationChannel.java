package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.model.Notification;

/**
 * Interface defining contract for different notification delivery channels.
 * This interface establishes the standard operations that all notification
 * channels must implement, ensuring consistent behavior across different
 * delivery mechanisms like email, SMS, push notifications, etc.
 * 
 * <p>Each implementation of this interface represents a specific channel
 * through which notifications can be delivered to users.</p>
 * 
 * @author gradlemedium200
 */
public interface NotificationChannel {

    /**
     * Sends a notification through this channel.
     * 
     * <p>Implementations should handle the actual delivery process and update
     * the notification status accordingly. Any channel-specific formatting or
     * processing should be performed within this method.</p>
     * 
     * @param notification The notification to be sent
     * @return true if the notification was successfully sent, false otherwise
     */
    boolean send(Notification notification);
    
    /**
     * Validates if the notification can be sent through this channel.
     * 
     * <p>This method should check that the notification contains all required
     * information for this specific channel and adheres to any channel-specific
     * constraints or limitations.</p>
     * 
     * @param notification The notification to validate
     * @return true if the notification can be sent through this channel, false otherwise
     */
    boolean validate(Notification notification);
    
    /**
     * Returns the name of this notification channel.
     * 
     * <p>The channel name should be a unique identifier that distinguishes
     * this channel from others in the system.</p>
     * 
     * @return The name of this notification channel
     */
    String getChannelName();
    
    /**
     * Checks if this channel is currently enabled.
     * 
     * <p>A disabled channel should not process notifications. This allows
     * for dynamically enabling or disabling channels without code changes.</p>
     * 
     * @return true if this channel is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Returns the maximum number of retry attempts for this channel.
     * 
     * <p>Different channels may have different retry policies based on
     * reliability, cost, and other factors. This method returns the maximum
     * number of times a failed notification should be retried on this channel.</p>
     * 
     * @return The maximum number of retry attempts for this channel
     */
    int getMaxRetryAttempts();
    
    /**
     * Default method that determines if a notification should be retried
     * based on its retry count and this channel's maximum retry attempts.
     * 
     * <p>This is a convenience method that implementations can use to determine
     * if a failed notification should be retried.</p>
     * 
     * @param notification The notification to check
     * @return true if the notification should be retried, false otherwise
     */
    default boolean shouldRetry(Notification notification) {
        // Don't retry if already at or beyond max attempts
        if (notification == null) {
            return false;
        }
        return notification.getRetryCount() < getMaxRetryAttempts();
    }
    
    /**
     * Default method that returns the priority level of this channel.
     * Higher values indicate higher priority.
     * 
     * <p>This can be used when deciding which channel to use when multiple
     * are available for a notification.</p>
     * 
     * @return The priority level of this channel (default is 0)
     */
    default int getPriority() {
        // Default implementation returns standard priority
        // Channel-specific implementations can override this
        return 0;
    }
}