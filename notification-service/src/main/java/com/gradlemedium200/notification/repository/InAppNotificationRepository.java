package com.gradlemedium200.notification.repository;

import com.gradlemedium200.notification.model.InAppNotification;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for in-app notification data access operations including read status management 
 * and user-specific queries. This interface defines specialized operations for handling notifications
 * that are displayed within the application interface.
 *
 * @author gradlemedium200
 */
public interface InAppNotificationRepository {
    
    /**
     * Saves an in-app notification to the repository.
     * If the notification has no ID, a new record is created.
     * If the notification has an existing ID, the record is updated.
     *
     * @param notification The in-app notification to save
     * @return The saved notification with populated ID if it was newly created
     * @throws IllegalArgumentException if notification is null
     */
    InAppNotification save(InAppNotification notification);
    
    /**
     * Finds an in-app notification by its unique identifier.
     *
     * @param id The notification ID to search for
     * @return Optional containing the notification if found, empty otherwise
     * @throws IllegalArgumentException if id is null or empty
     */
    Optional<InAppNotification> findById(String id);
    
    /**
     * Finds all in-app notifications for a specific recipient.
     *
     * @param recipientId The recipient's unique identifier
     * @return List of notifications for the recipient, empty list if none found
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    List<InAppNotification> findByRecipientId(String recipientId);
    
    /**
     * Finds all unread in-app notifications for a specific recipient.
     * This is useful for displaying notification badges and counters.
     *
     * @param recipientId The recipient's unique identifier
     * @return List of unread notifications for the recipient, empty list if none found
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    List<InAppNotification> findUnreadByRecipientId(String recipientId);
    
    /**
     * Marks an in-app notification as read.
     * This is a specialized operation for read status updates.
     *
     * @param id The notification ID to mark as read
     * @throws IllegalArgumentException if id is null or empty
     */
    void markAsRead(String id);
    
    /**
     * Marks all in-app notifications as read for a specific recipient.
     * This is useful for "mark all as read" functionality in the UI.
     *
     * @param recipientId The recipient's unique identifier
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    void markAllAsRead(String recipientId);
    
    /**
     * Finds in-app notifications by category for a specific recipient.
     * This allows filtering notifications based on their category.
     *
     * @param category The notification category to filter by
     * @param recipientId The recipient's unique identifier
     * @return List of notifications matching the category for the recipient
     * @throws IllegalArgumentException if category or recipientId is null or empty
     */
    List<InAppNotification> findByCategory(String category, String recipientId);
    
    /**
     * Deletes all expired in-app notifications and returns the count of deleted items.
     * This method should be called periodically to clean up old notifications.
     *
     * @return The number of expired notifications that were deleted
     */
    int deleteExpiredNotifications();
    
    /**
     * Finds all high priority in-app notifications for a specific recipient.
     * This is useful for highlighting important notifications.
     *
     * @param recipientId The recipient's unique identifier
     * @return List of high priority notifications for the recipient
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    default List<InAppNotification> findHighPriorityByRecipientId(String recipientId) {
        // TODO: Implement a more efficient query in concrete implementations
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /**
     * Counts unread in-app notifications for a specific recipient.
     * This is useful for displaying notification badges and counters.
     *
     * @param recipientId The recipient's unique identifier
     * @return Count of unread notifications for the recipient
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    default long countUnreadByRecipientId(String recipientId) {
        return findUnreadByRecipientId(recipientId).size();
    }
    
    /**
     * Marks an in-app notification as unread.
     * This allows users to mark important notifications for later review.
     *
     * @param id The notification ID to mark as unread
     * @throws IllegalArgumentException if id is null or empty
     */
    default void markAsUnread(String id) {
        // FIXME: This should be implemented more efficiently in concrete implementations
        Optional<InAppNotification> notification = findById(id);
        if (notification.isPresent()) {
            InAppNotification n = notification.get();
            n.markAsUnread();
            save(n);
        }
    }
}