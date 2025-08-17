package com.gradlemedium200.notification.model;

/**
 * Enumeration defining different types of notifications supported by the system.
 * <p>
 * This enum represents the various categories of notifications that can be sent
 * through the notification service, allowing for appropriate handling and routing
 * based on the notification type.
 * </p>
 * 
 * @author gradlemedium200
 */
public enum NotificationType {
    
    /**
     * Notification sent to confirm a user's order has been received and is being processed.
     * Typically contains order details, total price, and estimated delivery time.
     */
    ORDER_CONFIRMATION,
    
    /**
     * Notification providing updates about the shipping status of an order.
     * May include tracking information, carrier details, and estimated delivery date.
     */
    SHIPPING_UPDATE,
    
    /**
     * Promotional or marketing notification about sales, discounts, or new products.
     * These notifications are typically opt-in and subject to user preferences.
     */
    PROMOTIONAL,
    
    /**
     * System-wide alert notification about maintenance, outages, or important updates.
     * These are typically high-priority notifications that may override user preferences.
     */
    SYSTEM_ALERT,
    
    /**
     * Notification sent when a new user completes the registration process.
     * Usually contains welcome information and account verification details.
     */
    USER_REGISTRATION,
    
    /**
     * Notification sent during the password reset process.
     * Contains secure token or link for password reset functionality.
     * 
     * @see com.gradlemedium200.notification.service.NotificationService
     */
    PASSWORD_RESET,
    
    /**
     * Email notification type.
     * Used for notifications sent via email.
     */
    EMAIL;
    
    /**
     * Gets a string representation of this notification type.
     * 
     * @return A string representation of this notification type
     */
    @Override
    public String toString() {
        // Convert enum name from uppercase with underscores to a more readable format
        // e.g., ORDER_CONFIRMATION -> "Order Confirmation"
        String name = name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder(name.length());
        boolean capitalizeNext = true;
        
        for (char c : name.toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                capitalizeNext = c == ' ';
            }
        }
        
        return result.toString();
    }
    
    /**
     * Determines if this notification type requires immediate delivery.
     * System alerts and password resets are considered high priority and should be
     * delivered through the fastest available channels.
     * 
     * @return true if this is a high-priority notification type, false otherwise
     */
    public boolean isHighPriority() {
        return this == SYSTEM_ALERT || this == PASSWORD_RESET;
    }
    
    /**
     * Determines if this notification type is related to marketing.
     * Marketing notifications may be subject to different delivery rules
     * based on user preferences and compliance requirements.
     * 
     * @return true if this is a marketing-related notification type
     */
    public boolean isMarketing() {
        return this == PROMOTIONAL;
    }
    
    /**
     * Determines if this notification type is related to order processing.
     * 
     * @return true if this notification is related to orders
     */
    public boolean isOrderRelated() {
        return this == ORDER_CONFIRMATION || this == SHIPPING_UPDATE;
    }
    
    // Note: Java enum implicitly provides static methods:
    // values() -> NotificationType[]: Returns all enum values
    // valueOf(String name) -> NotificationType: Returns enum value by name
}