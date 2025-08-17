package com.gradlemedium200.orderservice.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data transfer object for notification messages.
 * This DTO contains all necessary information for sending notifications
 * related to order events through the notification service.
 */
public class NotificationDto {

    /**
     * The identifier of the notification recipient
     */
    private String recipientId;
    
    /**
     * The subject line of the notification
     */
    private String subject;
    
    /**
     * The full content of the notification message
     */
    private String message;
    
    /**
     * The type of notification (e.g., ORDER_CONFIRMATION, STATUS_UPDATE)
     */
    private String notificationType;
    
    /**
     * The identifier of the related order
     */
    private String orderId;
    
    /**
     * Timestamp when the notification was created
     */
    private LocalDateTime timestamp;
    
    /**
     * Additional attributes for the notification
     */
    private java.util.Map<String, String> attributes = new java.util.HashMap<>();

    /**
     * Default constructor
     */
    public NotificationDto() {
        // Default constructor required for serialization/deserialization
    }

    /**
     * Constructor with all fields
     * 
     * @param recipientId      Recipient identifier for notification
     * @param subject          Notification subject
     * @param message          Notification message content
     * @param notificationType Type of notification
     * @param orderId          Related order identifier
     * @param timestamp        Notification timestamp
     */
    public NotificationDto(String recipientId, String subject, String message, 
                          String notificationType, String orderId, LocalDateTime timestamp) {
        this.recipientId = recipientId;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.orderId = orderId;
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor for common notification scenarios
     * 
     * @param recipientId      Recipient identifier for notification
     * @param subject          Notification subject
     * @param message          Notification message content
     * @param notificationType Type of notification
     * @param orderId          Related order identifier
     */
    public NotificationDto(String recipientId, String subject, String message, 
                           String notificationType, String orderId) {
        this.recipientId = recipientId;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.orderId = orderId;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Validates that the notification has all required fields and meets business rules.
     * 
     * @return true if the notification is valid, false otherwise
     */
    public boolean validate() {
        // Check for required fields
        if (recipientId == null || recipientId.trim().isEmpty()) {
            return false;
        }
        
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }
        
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        if (notificationType == null || notificationType.trim().isEmpty()) {
            return false;
        }
        
        // orderId can be optional for some notification types
        if (notificationType.contains("ORDER") && (orderId == null || orderId.trim().isEmpty())) {
            return false;
        }
        
        // Timestamp should not be null and should not be in the future
        if (timestamp == null || timestamp.isAfter(LocalDateTime.now())) {
            return false;
        }
        
        // Add additional business validation rules as needed
        
        // Check subject length constraints
        if (subject.length() > 100) {
            return false;
        }
        
        // FIXME: Add validation for proper notification type values
        // TODO: Consider using an enum for notification types instead of String
        
        return true;
    }

    // Getters and Setters

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the customer ID (alias for recipientId)
     * 
     * @return The customer ID
     */
    public String getCustomerId() {
        return this.recipientId;
    }
    
    /**
     * Sets the customer ID (alias for recipientId)
     * 
     * @param customerId The customer ID
     */
    public void setCustomerId(String customerId) {
        this.recipientId = customerId;
    }
    
    /**
     * Gets the notification attributes
     * 
     * @return The map of attributes
     */
    public java.util.Map<String, String> getAttributes() {
        return this.attributes;
    }
    
    /**
     * Sets the notification attributes
     * 
     * @param attributes The map of attributes
     */
    public void setAttributes(java.util.Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * Adds an attribute to the notification
     * 
     * @param key The attribute key
     * @param value The attribute value
     */
    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new java.util.HashMap<>();
        }
        this.attributes.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        NotificationDto that = (NotificationDto) o;
        
        return Objects.equals(recipientId, that.recipientId) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(message, that.message) &&
               Objects.equals(notificationType, that.notificationType) &&
               Objects.equals(orderId, that.orderId) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientId, subject, message, notificationType, orderId, timestamp);
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "recipientId='" + recipientId + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + (message != null ? message.substring(0, Math.min(message.length(), 20)) + "..." : null) + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", orderId='" + orderId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Builder pattern implementation for NotificationDto
     */
    public static class Builder {
        private String recipientId;
        private String subject;
        private String message;
        private String notificationType;
        private String orderId;
        private LocalDateTime timestamp;

        public Builder withRecipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withNotificationType(String notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withCurrentTimestamp() {
            this.timestamp = LocalDateTime.now();
            return this;
        }

        public NotificationDto build() {
            return new NotificationDto(recipientId, subject, message, notificationType, orderId, 
                timestamp != null ? timestamp : LocalDateTime.now());
        }
    }
}