package com.gradlemedium200.notification.repository;

import com.gradlemedium200.notification.model.NotificationPreference;
import com.gradlemedium200.notification.model.NotificationType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for user notification preference data access operations.
 * <p>
 * This interface defines methods for managing user notification preferences,
 * including saving, retrieving, updating and deleting preferences. It also supports
 * querying preferences based on user ID, notification type, and enabled channels.
 * </p>
 *
 * @author gradlemedium200
 */
public interface NotificationPreferenceRepository {

    /**
     * Saves a notification preference.
     *
     * @param preference the notification preference to save
     * @return the saved notification preference with any system-generated fields populated
     */
    NotificationPreference save(NotificationPreference preference);
    
    /**
     * Finds a notification preference by its ID.
     *
     * @param id the unique identifier of the preference to find
     * @return an Optional containing the preference if found, or empty if not found
     */
    Optional<NotificationPreference> findById(String id);
    
    /**
     * Finds all notification preferences for a specific user.
     *
     * @param userId the ID of the user whose preferences to retrieve
     * @return a list of notification preferences for the user
     */
    List<NotificationPreference> findByUserId(String userId);
    
    /**
     * Finds a specific notification preference for a user and notification type.
     *
     * @param userId the ID of the user whose preference to retrieve
     * @param type the notification type to find preferences for
     * @return an Optional containing the preference if found, or empty if not found
     */
    Optional<NotificationPreference> findByUserIdAndType(String userId, NotificationType type);
    
    /**
     * Updates an existing notification preference.
     * 
     * @param preference the notification preference to update
     * @return the updated notification preference
     * @throws IllegalArgumentException if the preference doesn't exist
     */
    NotificationPreference updatePreference(NotificationPreference preference);
    
    /**
     * Deletes all notification preferences for a specific user.
     *
     * @param userId the ID of the user whose preferences to delete
     */
    void deleteByUserId(String userId);
    
    /**
     * Finds users who have enabled a specific notification channel for a notification type.
     * <p>
     * This method is useful for targeting notifications to users who have opted in
     * to receive specific types of notifications through particular channels.
     * </p>
     *
     * @param channel the notification channel to check (e.g., "EMAIL", "SMS", "PUSH", "IN_APP")
     * @param type the notification type to check
     * @return a list of user IDs who have enabled the specified channel for the notification type
     * 
     * TODO: Add pagination support for large result sets
     */
    List<String> findUsersWithEnabledChannel(String channel, NotificationType type);
}