package com.gradlemedium200.notification.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model representing user preferences for notification channels and types including
 * opt-in/opt-out settings and delivery schedules.
 * <p>
 * This class manages a user's notification preferences across multiple channels
 * (email, SMS, push, in-app) and supports delivery time restrictions through
 * quiet hours configuration.
 * </p>
 * 
 * @author gradlemedium200
 */
public class NotificationPreference {
    
    // Core identification fields
    private String id;
    private String userId;
    private NotificationType notificationType;
    
    // Channel preference flags
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;
    
    // Delivery schedule fields
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String timezone;
    private String frequency;
    
    /**
     * Default constructor for serialization frameworks
     */
    public NotificationPreference() {
        // Default constructor required for JPA and serialization
    }
    
    /**
     * Constructs a notification preference with required fields
     * 
     * @param userId the user ID these preferences belong to
     * @param notificationType the type of notification these preferences apply to
     */
    public NotificationPreference(String userId, NotificationType notificationType) {
        this.userId = userId;
        this.notificationType = notificationType;
        // Default to all channels enabled
        this.emailEnabled = true;
        this.smsEnabled = true;
        this.pushEnabled = true;
        this.inAppEnabled = true;
        // Default frequency is immediate
        this.frequency = "IMMEDIATE";
    }
    
    /**
     * Gets the preference ID
     * 
     * @return the unique identifier for the preference record
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the preference ID
     * 
     * @param id the unique identifier for the preference record
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Gets the user ID
     * 
     * @return the ID of the user these preferences belong to
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID
     * 
     * @param userId the ID of the user these preferences belong to
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Gets the notification type
     * 
     * @return the type of notification this preference applies to
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    /**
     * Sets the notification type
     * 
     * @param notificationType the type of notification this preference applies to
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    /**
     * Checks if email notifications are enabled
     * 
     * @return true if email notifications are enabled, false otherwise
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    /**
     * Sets whether email notifications are enabled
     * 
     * @param emailEnabled true to enable email notifications, false to disable
     */
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
    
    /**
     * Checks if SMS notifications are enabled
     * 
     * @return true if SMS notifications are enabled, false otherwise
     */
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    /**
     * Sets whether SMS notifications are enabled
     * 
     * @param smsEnabled true to enable SMS notifications, false to disable
     */
    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
    
    /**
     * Checks if push notifications are enabled
     * 
     * @return true if push notifications are enabled, false otherwise
     */
    public boolean isPushEnabled() {
        return pushEnabled;
    }
    
    /**
     * Sets whether push notifications are enabled
     * 
     * @param pushEnabled true to enable push notifications, false to disable
     */
    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }
    
    /**
     * Checks if in-app notifications are enabled
     * 
     * @return true if in-app notifications are enabled, false otherwise
     */
    public boolean isInAppEnabled() {
        return inAppEnabled;
    }
    
    /**
     * Sets whether in-app notifications are enabled
     * 
     * @param inAppEnabled true to enable in-app notifications, false to disable
     */
    public void setInAppEnabled(boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }
    
    /**
     * Gets the start time for quiet hours
     * 
     * @return the start time for quiet hours
     */
    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }
    
    /**
     * Sets the start time for quiet hours
     * 
     * @param quietHoursStart the start time for quiet hours
     */
    public void setQuietHoursStart(LocalTime quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }
    
    /**
     * Gets the end time for quiet hours
     * 
     * @return the end time for quiet hours
     */
    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }
    
    /**
     * Sets the end time for quiet hours
     * 
     * @param quietHoursEnd the end time for quiet hours
     */
    public void setQuietHoursEnd(LocalTime quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }
    
    /**
     * Gets the user's timezone
     * 
     * @return the user's timezone for scheduling notifications
     */
    public String getTimezone() {
        return timezone;
    }
    
    /**
     * Sets the user's timezone
     * 
     * @param timezone the user's timezone for scheduling notifications
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    /**
     * Gets the notification frequency preference
     * 
     * @return the notification frequency preference (IMMEDIATE, DAILY, WEEKLY)
     */
    public String getFrequency() {
        return frequency;
    }
    
    /**
     * Sets the notification frequency preference
     * 
     * @param frequency the notification frequency preference (IMMEDIATE, DAILY, WEEKLY)
     */
    public void setFrequency(String frequency) {
        // FIXME: Add validation to ensure frequency is one of the valid values
        this.frequency = frequency;
    }
    
    /**
     * Checks if the current time is within quiet hours
     * 
     * @param currentTime the time to check
     * @return true if the time is within quiet hours, false otherwise
     */
    public boolean isWithinQuietHours(LocalTime currentTime) {
        // If quiet hours are not configured, return false
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        // Handle case where quiet hours cross midnight
        if (quietHoursStart.isAfter(quietHoursEnd)) {
            return !currentTime.isAfter(quietHoursEnd) || !currentTime.isBefore(quietHoursStart);
        }
        
        // Normal case: start time is before end time
        return !currentTime.isBefore(quietHoursStart) && !currentTime.isAfter(quietHoursEnd);
    }
    
    /**
     * Returns list of enabled notification channels
     * 
     * @return a list of string identifiers for enabled notification channels
     */
    public List<String> getEnabledChannels() {
        List<String> enabledChannels = new ArrayList<>();
        
        if (emailEnabled) {
            enabledChannels.add("EMAIL");
        }
        
        if (smsEnabled) {
            enabledChannels.add("SMS");
        }
        
        if (pushEnabled) {
            enabledChannels.add("PUSH");
        }
        
        if (inAppEnabled) {
            enabledChannels.add("IN_APP");
        }
        
        return enabledChannels;
    }
    
    /**
     * Checks if any notification channel is enabled
     * 
     * @return true if at least one channel is enabled, false otherwise
     */
    public boolean hasEnabledChannels() {
        return emailEnabled || smsEnabled || pushEnabled || inAppEnabled;
    }
    
    /**
     * Enables or disables all notification channels
     * 
     * @param enabled true to enable all channels, false to disable all
     */
    public void setAllChannels(boolean enabled) {
        this.emailEnabled = enabled;
        this.smsEnabled = enabled;
        this.pushEnabled = enabled;
        this.inAppEnabled = enabled;
    }
    
    /**
     * Convenience method to set quiet hours in one call
     * 
     * @param start the start time for quiet hours
     * @param end the end time for quiet hours
     */
    public void setQuietHours(LocalTime start, LocalTime end) {
        this.quietHoursStart = start;
        this.quietHoursEnd = end;
    }
    
    /**
     * Determines if this preference allows notifications of high priority 
     * regardless of quiet hours.
     * 
     * @return true if high priority notifications bypass quiet hours
     * 
     * TODO: Add configuration option for users to specify if high priority
     * notifications should bypass quiet hours
     */
    public boolean allowsHighPriorityDuringQuietHours() {
        return notificationType != null && notificationType.isHighPriority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationPreference that = (NotificationPreference) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(userId, that.userId) &&
               notificationType == that.notificationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, notificationType);
    }

    @Override
    public String toString() {
        return "NotificationPreference{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", notificationType=" + notificationType +
                ", channels=[" + 
                (emailEnabled ? "EMAIL," : "") +
                (smsEnabled ? "SMS," : "") +
                (pushEnabled ? "PUSH," : "") +
                (inAppEnabled ? "IN_APP" : "") +
                "], frequency='" + frequency + '\'' +
                '}';
    }
}