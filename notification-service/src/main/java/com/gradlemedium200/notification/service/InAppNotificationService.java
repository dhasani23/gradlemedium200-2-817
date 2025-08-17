package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.model.InAppNotification;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;
import com.gradlemedium200.notification.repository.InAppNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for handling in-app notifications stored in database for 
 * real-time user interface updates with read status management
 */
@Service
public class InAppNotificationService implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(InAppNotificationService.class);
    
    private final InAppNotificationRepository inAppNotificationRepository;
    
    @Value("${notification.inapp.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${notification.inapp.enabled:true}")
    private boolean enabled;
    
    @Value("${notification.inapp.default-expiration-hours:168}") // Default 7 days
    private int defaultExpirationHours;

    @Autowired
    public InAppNotificationService(InAppNotificationRepository inAppNotificationRepository) {
        this.inAppNotificationRepository = inAppNotificationRepository;
    }

    /**
     * Stores an in-app notification in the database
     * 
     * @param notification The notification to be stored
     * @return true if the notification was stored successfully, false otherwise
     */
    @Override
    public boolean send(Notification notification) {
        if (!enabled) {
            logger.warn("In-app notification channel is disabled. Skipping notification: {}", notification.getId());
            return false;
        }
        
        if (!validate(notification)) {
            logger.error("Invalid in-app notification: {}", notification.getId());
            return false;
        }
        
        try {
            InAppNotification inAppNotification = createInAppNotification(notification);
            inAppNotificationRepository.save(inAppNotification);
            logger.info("In-app notification stored successfully: {}", inAppNotification.getId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to store in-app notification: {}", notification.getId(), e);
            return false;
        }
    }

    /**
     * Validates in-app notification before storing
     * 
     * @param notification The notification to validate
     * @return true if the notification is valid, false otherwise
     */
    @Override
    public boolean validate(Notification notification) {
        if (notification == null) {
            logger.error("Notification cannot be null");
            return false;
        }
        
        if (notification.getRecipientId() == null || notification.getRecipientId().trim().isEmpty()) {
            logger.error("Recipient ID cannot be null or empty for in-app notification");
            return false;
        }
        
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            logger.error("Message content cannot be null or empty for in-app notification");
            return false;
        }
        
        return true;
    }

    /**
     * Creates in-app notification from generic notification
     * 
     * @param notification The generic notification
     * @return A new InAppNotification object
     */
    public InAppNotification createInAppNotification(Notification notification) {
        InAppNotification inAppNotification;
        
        if (notification instanceof InAppNotification) {
            inAppNotification = (InAppNotification) notification;
        } else {
            inAppNotification = new InAppNotification();
            inAppNotification.setId(notification.getId() != null ? notification.getId() : UUID.randomUUID().toString());
            inAppNotification.setRecipientId(notification.getRecipientId());
            inAppNotification.setMessage(notification.getMessage());
            inAppNotification.setSubject(notification.getSubject());
            inAppNotification.setType(notification.getType());
            inAppNotification.setCreatedAt(notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now());
            inAppNotification.setStatus(NotificationStatus.DELIVERED); // In-app notifications are considered delivered once stored
        }
        
        // Set default values if not already set
        if (inAppNotification.getPriority() == 0) {
            inAppNotification.setPriority(1); // Medium priority
        }
        
        // Set expiration date if not already set
        if (inAppNotification.getExpiresAt() == null) {
            inAppNotification.setExpiresAt(LocalDateTime.now().plusHours(defaultExpirationHours));
        }
        
        return inAppNotification;
    }

    /**
     * Gets all unread notifications for a user
     * 
     * @param recipientId The ID of the recipient
     * @return List of unread notifications
     */
    public List<InAppNotification> getUnreadNotifications(String recipientId) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            logger.error("Recipient ID cannot be null or empty");
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        logger.debug("Fetching unread notifications for recipient: {}", recipientId);
        return inAppNotificationRepository.findUnreadByRecipientId(recipientId);
    }

    /**
     * Marks a notification as read
     * 
     * @param notificationId The ID of the notification to mark as read
     * @return true if the notification was marked as read, false otherwise
     */
    public boolean markAsRead(String notificationId) {
        if (notificationId == null || notificationId.trim().isEmpty()) {
            logger.error("Notification ID cannot be null or empty");
            return false;
        }
        
        try {
            inAppNotificationRepository.markAsRead(notificationId);
            logger.debug("Notification marked as read: {}", notificationId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to mark notification as read: {}", notificationId, e);
            return false;
        }
    }

    /**
     * Marks all notifications as read for a user
     * 
     * @param recipientId The ID of the recipient
     */
    public void markAllAsRead(String recipientId) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            logger.error("Recipient ID cannot be null or empty");
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        try {
            inAppNotificationRepository.markAllAsRead(recipientId);
            logger.info("All notifications marked as read for recipient: {}", recipientId);
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for recipient: {}", recipientId, e);
            throw e;
        }
    }

    /**
     * Gets notifications by category for a user
     * 
     * @param recipientId The ID of the recipient
     * @param category The category of notifications to retrieve
     * @return List of notifications in the specified category
     */
    public List<InAppNotification> getNotificationsByCategory(String recipientId, String category) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            logger.error("Recipient ID cannot be null or empty");
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        if (category == null || category.trim().isEmpty()) {
            logger.error("Category cannot be null or empty");
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        logger.debug("Fetching notifications for recipient: {} and category: {}", recipientId, category);
        return inAppNotificationRepository.findByCategory(category, recipientId);
    }

    /**
     * Removes expired notifications and returns count
     * 
     * @return The number of expired notifications removed
     */
    public int cleanupExpiredNotifications() {
        try {
            int count = inAppNotificationRepository.deleteExpiredNotifications();
            logger.info("Cleaned up {} expired in-app notifications", count);
            return count;
        } catch (Exception e) {
            logger.error("Failed to clean up expired notifications", e);
            // FIXME: Consider implementing a more robust retry mechanism for cleanup operations
            return 0;
        }
    }

    /**
     * Returns the name of the in-app notification channel
     * 
     * @return The channel name
     */
    @Override
    public String getChannelName() {
        return "IN_APP";
    }

    /**
     * Checks if this channel is currently enabled
     * 
     * @return true if the channel is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the maximum number of retry attempts for this channel
     * 
     * @return The maximum number of retry attempts
     */
    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
}