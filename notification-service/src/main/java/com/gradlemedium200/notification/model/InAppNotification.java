package com.gradlemedium200.notification.model;

import java.time.LocalDateTime;

/**
 * Represents an in-app notification that will be displayed to users within the application.
 * These notifications appear in the notification center or as banners/toasts in the UI.
 */
public class InAppNotification extends Notification {
    
    private String title;
    private String iconUrl;
    private String actionUrl;
    private boolean read;
    private LocalDateTime readTimestamp;
    private String category;
    private int priority;
    
    private LocalDateTime expiresAt;
    
    /**
     * Default constructor.
     */
    public InAppNotification() {
        super();
        this.setType(NotificationType.SYSTEM_ALERT); // Default type
        this.read = false;
        this.priority = 0; // Normal priority
    }
    
    /**
     * Constructs an in-app notification with essential fields.
     * 
     * @param id Unique identifier
     * @param recipientId ID of the user who will receive this notification
     * @param message Notification message
     * @param title Short title of the notification
     */
    public InAppNotification(String id, String recipientId, String message, String title) {
        super(id, recipientId, message);
        this.title = title;
        this.setType(NotificationType.SYSTEM_ALERT);
        this.read = false;
        this.priority = 0;
    }
    
    /**
     * Gets the notification title.
     * 
     * @return The notification title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the notification title.
     * 
     * @param title The notification title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the URL of the icon to display with this notification.
     * 
     * @return The icon URL
     */
    public String getIconUrl() {
        return iconUrl;
    }
    
    /**
     * Sets the URL of the icon to display with this notification.
     * 
     * @param iconUrl The icon URL to set
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    /**
     * Gets the action URL that will be navigated to when the notification is clicked.
     * 
     * @return The action URL
     */
    public String getActionUrl() {
        return actionUrl;
    }
    
    /**
     * Sets the action URL that will be navigated to when the notification is clicked.
     * 
     * @param actionUrl The action URL to set
     */
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    /**
     * Checks if this notification has been read by the user.
     * 
     * @return true if the notification has been read, false otherwise
     */
    public boolean isRead() {
        return read;
    }
    
    /**
     * Sets whether this notification has been read by the user.
     * 
     * @param read The read status to set
     */
    public void setRead(boolean read) {
        this.read = read;
        if (read && readTimestamp == null) {
            readTimestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Gets the timestamp when this notification was marked as read.
     * 
     * @return The read timestamp, or null if not read
     */
    public LocalDateTime getReadTimestamp() {
        return readTimestamp;
    }
    
    /**
     * Sets the timestamp when this notification was marked as read.
     * 
     * @param readTimestamp The read timestamp to set
     */
    public void setReadTimestamp(LocalDateTime readTimestamp) {
        this.readTimestamp = readTimestamp;
    }
    
    /**
     * Gets the category of this notification.
     * Categories help group related notifications together.
     * 
     * @return The notification category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the category of this notification.
     * 
     * @param category The notification category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Gets the priority level of this notification.
     * Higher numbers indicate higher priority.
     * 
     * @return The priority level
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Sets the priority level of this notification.
     * 
     * @param priority The priority level to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * Gets the expiration time of this notification.
     * 
     * @return The expiration time, or null if it doesn't expire
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    /**
     * Sets the expiration time of this notification.
     * 
     * @param expiresAt The expiration time to set
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    /**
     * Marks the notification as unread.
     */
    public void markAsUnread() {
        this.setRead(false);
        this.readTimestamp = null;
    }
    
    @Override
    public String toString() {
        return "InAppNotification{" +
                "id='" + getId() + '\'' +
                ", recipientId='" + getRecipientId() + '\'' +
                ", title='" + title + '\'' +
                ", message='" + getMessage() + '\'' +
                ", status=" + getStatus() +
                ", read=" + read +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", createdAt=" + getCreatedAt() +
                ", sentAt=" + getSentAt() +
                '}';
    }
}