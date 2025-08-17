package com.gradlemedium200.notification.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base notification model representing common properties of all notification types.
 * This class serves as the foundational data structure for the notification service,
 * containing all essential fields required for notification tracking and delivery.
 * 
 * @author gradlemedium200
 */
public class Notification {
    
    /**
     * Unique identifier for the notification
     */
    private String id;
    
    /**
     * ID of the notification recipient
     */
    private String recipientId;
    
    /**
     * Notification message content
     */
    private String message;
    
    /**
     * Notification subject or title
     */
    private String subject;
    
    /**
     * Current delivery status of the notification
     */
    private NotificationStatus status;
    
    /**
     * Type classification of the notification
     */
    private NotificationType type;
    
    /**
     * Timestamp when notification was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when notification was sent
     */
    private LocalDateTime sentAt;
    
    /**
     * Number of delivery retry attempts
     */
    private int retryCount;

    /**
     * Default constructor initializing a notification with default values.
     * Sets status to PENDING and retry count to 0.
     */
    public Notification() {
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Parameterized constructor for creating a notification with essential fields.
     *
     * @param id The unique identifier
     * @param recipientId The recipient's identifier
     * @param message The notification message content
     * @param subject The notification subject
     * @param type The notification type
     */
    public Notification(String id, String recipientId, String message, String subject, NotificationType type) {
        this();
        this.id = id;
        this.recipientId = recipientId;
        this.message = message;
        this.subject = subject;
        this.type = type;
    }
    
    /**
     * Parameterized constructor for creating a notification with basic fields.
     *
     * @param id The unique identifier
     * @param recipientId The recipient's identifier
     * @param message The notification message content
     */
    public Notification(String id, String recipientId, String message) {
        this();
        this.id = id;
        this.recipientId = recipientId;
        this.message = message;
    }

    /**
     * Gets the notification ID
     *
     * @return The notification ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the notification ID
     *
     * @param id The notification ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the recipient ID
     *
     * @return The recipient ID
     */
    public String getRecipientId() {
        return recipientId;
    }

    /**
     * Sets the recipient ID
     *
     * @param recipientId The recipient ID
     */
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * Gets the notification message
     *
     * @return The notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message
     *
     * @param message The notification message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the notification subject
     *
     * @return The notification subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the notification subject
     *
     * @param subject The notification subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the notification status
     *
     * @return The notification status
     */
    public NotificationStatus getStatus() {
        return status;
    }

    /**
     * Sets the notification status
     * When status changes to SENT, updates the sentAt timestamp
     *
     * @param status The notification status
     */
    public void setStatus(NotificationStatus status) {
        // Record sent timestamp when status changes to SENT
        if (status == NotificationStatus.SENT && this.status != NotificationStatus.SENT) {
            this.sentAt = LocalDateTime.now();
        }
        this.status = status;
    }

    /**
     * Gets the notification type
     *
     * @return The notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets the notification type
     *
     * @param type The notification type
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * Gets the creation timestamp
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp
     *
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the sent timestamp
     *
     * @return The sent timestamp
     */
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Sets the sent timestamp
     *
     * @param sentAt The sent timestamp
     */
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Gets the retry count
     *
     * @return The retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the retry count
     *
     * @param retryCount The retry count
     */
    public void setRetryCount(int retryCount) {
        // Ensure retry count is never negative
        this.retryCount = Math.max(0, retryCount);
    }

    /**
     * Increments the retry count
     * If the retry count exceeds a threshold, the status is updated to FAILED
     */
    public void incrementRetryCount() {
        this.retryCount++;
        
        // TODO: Make max retry count configurable via application properties
        if (this.retryCount >= 5) {
            // FIXME: Consider moving this logic to a service layer to avoid business logic in model
            this.setStatus(NotificationStatus.FAILED);
        } else if (this.status == NotificationStatus.FAILED) {
            // If we're incrementing retries but status was already FAILED, set to RETRY
            this.setStatus(NotificationStatus.RETRY);
        }
    }
    
    /**
     * Determines if this notification is high priority based on its type
     * 
     * @return true if this is a high-priority notification
     */
    public boolean isHighPriority() {
        return type != null && type.isHighPriority();
    }
    
    /**
     * Determines if this notification has been successfully delivered
     * 
     * @return true if the status is DELIVERED
     */
    public boolean isDelivered() {
        return status == NotificationStatus.DELIVERED;
    }
    
    /**
     * Calculates the age of this notification in seconds
     * 
     * @return The age in seconds since creation
     */
    public long getAgeInSeconds() {
        if (createdAt == null) {
            return 0;
        }
        return LocalDateTime.now().minusSeconds(createdAt.getSecond()).getSecond();
    }
    
    /**
     * Returns a string representation of this notification.
     *
     * @return A string representation of this notification
     */
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", subject='" + subject + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", retryCount=" + retryCount +
                '}';
    }

    /**
     * Compares this notification to the specified object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Returns a hash code value for this notification.
     *
     * @return A hash code value for this notification
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}