package com.gradlemedium200.notification.repository;

import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for notification data access operations.
 * Defines standard CRUD operations and specialized queries for notification management.
 * Implementations can be backed by various data stores (relational DB, NoSQL, etc.)
 */
public interface NotificationRepository {
    
    /**
     * Saves a notification to the repository.
     * If the notification has no ID, a new record is created.
     * If the notification has an existing ID, the record is updated.
     *
     * @param notification The notification to save
     * @return The saved notification with populated ID if it was newly created
     * @throws IllegalArgumentException if notification is null
     */
    Notification save(Notification notification);
    
    /**
     * Finds a notification by its unique identifier.
     *
     * @param id The notification ID to search for
     * @return Optional containing the notification if found, empty otherwise
     * @throws IllegalArgumentException if id is null or empty
     */
    Optional<Notification> findById(String id);
    
    /**
     * Finds all notifications for a specific recipient.
     *
     * @param recipientId The recipient's unique identifier
     * @return List of notifications for the recipient, empty list if none found
     * @throws IllegalArgumentException if recipientId is null or empty
     */
    List<Notification> findByRecipientId(String recipientId);
    
    /**
     * Finds all notifications with a specific status.
     * Used for filtering notifications based on their delivery state.
     *
     * @param status The notification status to filter by
     * @return List of notifications with the specified status, empty list if none found
     * @throws IllegalArgumentException if status is null
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * Updates the status of a notification.
     * This is a specialized operation for status updates to avoid full object updates.
     *
     * @param id The notification ID
     * @param status The new status to set
     * @throws IllegalArgumentException if id is null/empty or status is null
     * @throws javax.persistence.EntityNotFoundException if notification with given ID is not found
     */
    void updateStatus(String id, NotificationStatus status);
    
    /**
     * Finds all notifications with pending status.
     * This is a convenience method for frequently needed status filtering.
     *
     * @return List of pending notifications, empty list if none found
     */
    default List<Notification> findPendingNotifications() {
        return findByStatus(NotificationStatus.PENDING);
    }
    
    /**
     * Finds all notifications that failed delivery.
     * This is a convenience method for frequently needed status filtering.
     *
     * @return List of failed notifications, empty list if none found
     */
    default List<Notification> findFailedNotifications() {
        return findByStatus(NotificationStatus.FAILED);
    }
    
    /**
     * Deletes a notification by its ID.
     *
     * @param id The notification ID to delete
     * @throws IllegalArgumentException if id is null or empty
     */
    void deleteById(String id);
}