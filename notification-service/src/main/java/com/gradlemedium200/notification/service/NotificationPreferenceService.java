package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.model.NotificationPreference;
import com.gradlemedium200.notification.model.NotificationType;
import com.gradlemedium200.notification.repository.NotificationPreferenceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user notification preferences and channel selection logic
 * with support for quiet hours and frequency settings.
 * <p>
 * This service provides functionality for retrieving, updating, and evaluating
 * user notification preferences. It handles the business logic for determining
 * when and through which channels notifications should be delivered based on
 * user preferences.
 * </p>
 * 
 * @author gradlemedium200
 */
@Service
public class NotificationPreferenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceService.class);
    
    private final NotificationPreferenceRepository preferenceRepository;
    private final Map<NotificationType, NotificationPreference> defaultPreferences;
    
    /**
     * Constructs a new NotificationPreferenceService with required dependencies.
     * 
     * @param preferenceRepository repository for notification preferences
     */
    @Autowired
    public NotificationPreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
        this.defaultPreferences = initializeDefaultPreferences();
    }
    
    /**
     * Gets all notification preferences for a user.
     * 
     * @param userId the user ID to get preferences for
     * @return a list of notification preferences for the user
     */
    public List<NotificationPreference> getUserPreferences(String userId) {
        logger.debug("Retrieving all notification preferences for user: {}", userId);
        return preferenceRepository.findByUserId(userId);
    }
    
    /**
     * Gets preference for specific user and notification type.
     * 
     * @param userId the user ID to get preference for
     * @param type the notification type to get preference for
     * @return an Optional containing the preference if found, empty otherwise
     */
    public Optional<NotificationPreference> getUserPreference(String userId, NotificationType type) {
        logger.debug("Retrieving notification preference for user: {} and type: {}", userId, type);
        return preferenceRepository.findByUserIdAndType(userId, type);
    }
    
    /**
     * Updates a user's notification preference.
     * 
     * @param preference the notification preference to update
     * @return the updated notification preference
     */
    public NotificationPreference updatePreference(NotificationPreference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("Notification preference cannot be null");
        }
        
        if (preference.getUserId() == null || preference.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (preference.getNotificationType() == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        
        logger.info("Updating notification preference for user: {} and type: {}", 
                preference.getUserId(), preference.getNotificationType());
        return preferenceRepository.save(preference);
    }
    
    /**
     * Creates default preferences for a new user.
     * 
     * @param userId the user ID to create default preferences for
     * @return the list of created default preferences
     */
    public List<NotificationPreference> createDefaultPreferences(String userId) {
        logger.info("Creating default notification preferences for user: {}", userId);
        
        List<NotificationPreference> createdPreferences = new ArrayList<>();
        
        for (Map.Entry<NotificationType, NotificationPreference> entry : defaultPreferences.entrySet()) {
            NotificationType type = entry.getKey();
            NotificationPreference template = entry.getValue();
            
            NotificationPreference preference = new NotificationPreference(userId, type);
            preference.setEmailEnabled(template.isEmailEnabled());
            preference.setSmsEnabled(template.isSmsEnabled());
            preference.setPushEnabled(template.isPushEnabled());
            preference.setInAppEnabled(template.isInAppEnabled());
            
            if (template.getQuietHoursStart() != null && template.getQuietHoursEnd() != null) {
                preference.setQuietHours(template.getQuietHoursStart(), template.getQuietHoursEnd());
            }
            
            preference.setTimezone(template.getTimezone());
            preference.setFrequency(template.getFrequency());
            
            // Save the preference and add it to our result list
            createdPreferences.add(preferenceRepository.save(preference));
        }
        
        logger.debug("Created {} default preferences for user {}", createdPreferences.size(), userId);
        return createdPreferences;
    }
    
    /**
     * Checks if a specific channel is enabled for user and notification type.
     * 
     * @param userId the user ID to check preferences for
     * @param type the notification type to check
     * @param channel the channel to check if enabled
     * @return true if the channel is enabled, false otherwise
     */
    public boolean isChannelEnabled(String userId, NotificationType type, String channel) {
        Optional<NotificationPreference> preferenceOpt = getUserPreference(userId, type);
        
        if (!preferenceOpt.isPresent()) {
            // Fall back to default preferences if user doesn't have specific preferences
            NotificationPreference defaultPreference = defaultPreferences.get(type);
            if (defaultPreference == null) {
                logger.warn("No default preference found for notification type: {}", type);
                return false;
            }
            return isChannelEnabledForPreference(defaultPreference, channel);
        }
        
        return isChannelEnabledForPreference(preferenceOpt.get(), channel);
    }
    
    /**
     * Checks if current time is within user's quiet hours.
     * 
     * @param userId the user ID to check quiet hours for
     * @return true if current time is within quiet hours, false otherwise
     */
    public boolean isWithinQuietHours(String userId) {
        // Get the user's preferences to find common quiet hours
        List<NotificationPreference> preferences = getUserPreferences(userId);
        
        if (preferences.isEmpty()) {
            logger.debug("No preferences found for user {}, quiet hours not applicable", userId);
            return false;
        }
        
        // Find first preference with timezone specified
        String timezone = preferences.stream()
                .map(NotificationPreference::getTimezone)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("UTC");
        
        // Get current time in user's timezone
        LocalTime currentTime = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime();
        
        // Check if any preference has quiet hours that include the current time
        for (NotificationPreference pref : preferences) {
            if (pref.getQuietHoursStart() != null && pref.getQuietHoursEnd() != null) {
                if (pref.isWithinQuietHours(currentTime)) {
                    logger.debug("Current time {} is within quiet hours for user {}", currentTime, userId);
                    return true;
                }
            }
        }
        
        logger.debug("Current time {} is not within quiet hours for user {}", currentTime, userId);
        return false;
    }
    
    /**
     * Gets list of enabled channels for user and notification type.
     * 
     * @param userId the user ID to get enabled channels for
     * @param type the notification type to get enabled channels for
     * @return list of enabled channel names
     */
    public List<String> getEnabledChannels(String userId, NotificationType type) {
        Optional<NotificationPreference> preferenceOpt = getUserPreference(userId, type);
        
        if (!preferenceOpt.isPresent()) {
            // Fall back to default preferences if user doesn't have specific preferences
            NotificationPreference defaultPreference = defaultPreferences.get(type);
            if (defaultPreference == null) {
                logger.warn("No default preference found for notification type: {}", type);
                return Collections.emptyList();
            }
            return defaultPreference.getEnabledChannels();
        }
        
        return preferenceOpt.get().getEnabledChannels();
    }
    
    /**
     * Determines if notification should be sent based on preferences.
     * This method considers:
     * 1. If the channel is enabled
     * 2. If the current time is within quiet hours
     * 3. Special handling for high priority notifications
     *
     * @param userId the user ID to check preferences for
     * @param type the notification type to check
     * @param channel the channel to check if notification should be sent through
     * @return true if notification should be sent, false otherwise
     */
    public boolean shouldSendNotification(String userId, NotificationType type, String channel) {
        Optional<NotificationPreference> preferenceOpt = getUserPreference(userId, type);
        
        // If no preference exists, fall back to default preference
        NotificationPreference preference = preferenceOpt.orElseGet(() -> defaultPreferences.get(type));
        
        if (preference == null) {
            logger.warn("No preference found for user {} and notification type {}", userId, type);
            return false;
        }
        
        // Check if the channel is enabled
        if (!isChannelEnabledForPreference(preference, channel)) {
            logger.debug("Channel {} is disabled for user {} and type {}", channel, userId, type);
            return false;
        }
        
        // Check quiet hours - if within quiet hours and not high priority, don't send
        if (isWithinQuietHours(userId)) {
            boolean isHighPriority = type.isHighPriority();
            boolean allowsDuringQuietHours = preference.allowsHighPriorityDuringQuietHours();
            
            if (!isHighPriority || !allowsDuringQuietHours) {
                logger.debug("Within quiet hours and notification is not allowed: user={}, type={}, highPriority={}, allowsDuringQuietHours={}", 
                        userId, type, isHighPriority, allowsDuringQuietHours);
                return false;
            }
            
            logger.debug("Within quiet hours but notification is high priority and allowed: user={}, type={}", 
                    userId, type);
        }
        
        // Check frequency settings
        // FIXME: Implement frequency-based filtering for notifications
        // This would require tracking when last notification was sent
        
        return true;
    }
    
    /**
     * Deletes all preferences for a user.
     * 
     * @param userId the user ID to delete preferences for
     */
    public void deleteUserPreferences(String userId) {
        logger.info("Deleting all notification preferences for user: {}", userId);
        preferenceRepository.deleteByUserId(userId);
    }
    
    /**
     * Initialize the default preference settings for each notification type.
     * These default preferences are applied when a user has not explicitly
     * set their own preferences.
     *
     * @return a map of notification types to their default preferences
     */
    private Map<NotificationType, NotificationPreference> initializeDefaultPreferences() {
        Map<NotificationType, NotificationPreference> defaults = new HashMap<>();
        
        // Default for order confirmation - most channels enabled
        NotificationPreference orderConfirmation = new NotificationPreference("default", NotificationType.ORDER_CONFIRMATION);
        orderConfirmation.setEmailEnabled(true);
        orderConfirmation.setSmsEnabled(true);
        orderConfirmation.setPushEnabled(true);
        orderConfirmation.setInAppEnabled(true);
        orderConfirmation.setQuietHours(LocalTime.of(22, 0), LocalTime.of(8, 0));
        orderConfirmation.setTimezone("UTC");
        orderConfirmation.setFrequency("IMMEDIATE");
        defaults.put(NotificationType.ORDER_CONFIRMATION, orderConfirmation);
        
        // Default for shipping updates - most channels enabled
        NotificationPreference shippingUpdate = new NotificationPreference("default", NotificationType.SHIPPING_UPDATE);
        shippingUpdate.setEmailEnabled(true);
        shippingUpdate.setSmsEnabled(true);
        shippingUpdate.setPushEnabled(true);
        shippingUpdate.setInAppEnabled(true);
        shippingUpdate.setQuietHours(LocalTime.of(22, 0), LocalTime.of(8, 0));
        shippingUpdate.setTimezone("UTC");
        shippingUpdate.setFrequency("IMMEDIATE");
        defaults.put(NotificationType.SHIPPING_UPDATE, shippingUpdate);
        
        // Default for promotional notifications - limited channels, not intrusive
        NotificationPreference promotional = new NotificationPreference("default", NotificationType.PROMOTIONAL);
        promotional.setEmailEnabled(true);
        promotional.setSmsEnabled(false); // SMS not enabled by default for promotional content
        promotional.setPushEnabled(false); // Push not enabled by default for promotional content
        promotional.setInAppEnabled(true);
        promotional.setQuietHours(LocalTime.of(20, 0), LocalTime.of(9, 0)); // Wider quiet hours for promotional
        promotional.setTimezone("UTC");
        promotional.setFrequency("DAILY"); // Batch promotional messages
        defaults.put(NotificationType.PROMOTIONAL, promotional);
        
        // Default for system alerts - all channels, high priority
        NotificationPreference systemAlert = new NotificationPreference("default", NotificationType.SYSTEM_ALERT);
        systemAlert.setEmailEnabled(true);
        systemAlert.setSmsEnabled(true);
        systemAlert.setPushEnabled(true);
        systemAlert.setInAppEnabled(true);
        systemAlert.setTimezone("UTC");
        systemAlert.setFrequency("IMMEDIATE");
        // No quiet hours for system alerts as they are high priority
        defaults.put(NotificationType.SYSTEM_ALERT, systemAlert);
        
        // Default for user registration - welcome emails
        NotificationPreference registration = new NotificationPreference("default", NotificationType.USER_REGISTRATION);
        registration.setEmailEnabled(true);
        registration.setSmsEnabled(false);
        registration.setPushEnabled(false);
        registration.setInAppEnabled(true);
        registration.setTimezone("UTC");
        registration.setFrequency("IMMEDIATE");
        defaults.put(NotificationType.USER_REGISTRATION, registration);
        
        // Default for password reset - security focused
        NotificationPreference passwordReset = new NotificationPreference("default", NotificationType.PASSWORD_RESET);
        passwordReset.setEmailEnabled(true);
        passwordReset.setSmsEnabled(true);
        passwordReset.setPushEnabled(false);
        passwordReset.setInAppEnabled(false);
        passwordReset.setTimezone("UTC");
        passwordReset.setFrequency("IMMEDIATE");
        defaults.put(NotificationType.PASSWORD_RESET, passwordReset);
        
        return defaults;
    }
    
    /**
     * Helper method to check if a specific channel is enabled for a preference.
     *
     * @param preference the preference to check
     * @param channel the channel name to check
     * @return true if the channel is enabled, false otherwise
     */
    private boolean isChannelEnabledForPreference(NotificationPreference preference, String channel) {
        if (channel == null || channel.isEmpty()) {
            return false;
        }
        
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return preference.isEmailEnabled();
            case "SMS":
                return preference.isSmsEnabled();
            case "PUSH":
                return preference.isPushEnabled();
            case "IN_APP":
                return preference.isInAppEnabled();
            default:
                logger.warn("Unknown channel type: {}", channel);
                return false;
        }
    }
}