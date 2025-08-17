package com.gradlemedium200.notification.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Push notification-specific model with push notification properties including 
 * device tokens, platform-specific data, and badge counts.
 * This class extends the base Notification class to add push notification specific
 * properties and behaviors.
 * 
 * @author gradlemedium200
 */
public class PushNotification extends Notification {
    
    /**
     * Device token for push notification delivery
     */
    private String deviceToken;
    
    /**
     * Target platform (iOS, Android, Web)
     */
    private String platform;
    
    /**
     * Push notification title
     */
    private String title;
    
    /**
     * Push notification body text
     */
    private String body;
    
    /**
     * Badge count for the application
     */
    private Integer badge;
    
    /**
     * Sound file to play for the notification
     */
    private String sound;
    
    /**
     * Custom data payload for the push notification
     */
    private Map<String, String> customData;
    
    /**
     * Default constructor initializing a push notification with default values.
     */
    public PushNotification() {
        super();
        this.customData = new HashMap<>();
        this.badge = 0;
        // Default sound for notifications
        this.sound = "default";
    }
    
    /**
     * Parameterized constructor for creating a push notification with essential fields.
     *
     * @param id The unique identifier
     * @param recipientId The recipient's identifier
     * @param title The push notification title
     * @param body The push notification body text
     * @param deviceToken The device token for delivery
     * @param platform The target platform (iOS, Android, Web)
     * @param type The notification type
     */
    public PushNotification(String id, String recipientId, String title, String body,
                           String deviceToken, String platform, NotificationType type) {
        super(id, recipientId, body, title, type);
        this.deviceToken = deviceToken;
        this.platform = platform;
        this.title = title;
        this.body = body;
        this.badge = 0;
        this.sound = "default";
        this.customData = new HashMap<>();
    }
    
    /**
     * Gets the device token
     *
     * @return The device token
     */
    public String getDeviceToken() {
        return deviceToken;
    }
    
    /**
     * Sets the device token
     *
     * @param deviceToken The device token
     */
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    /**
     * Gets the target platform
     *
     * @return The target platform
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * Sets the target platform
     *
     * @param platform The target platform
     */
    public void setPlatform(String platform) {
        // Validate platform type
        if (platform != null && 
            !("iOS".equalsIgnoreCase(platform) || 
              "Android".equalsIgnoreCase(platform) || 
              "Web".equalsIgnoreCase(platform))) {
            throw new IllegalArgumentException("Platform must be iOS, Android, or Web");
        }
        this.platform = platform;
    }
    
    /**
     * Gets the push notification title
     *
     * @return The push notification title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the push notification title
     *
     * @param title The push notification title
     */
    public void setTitle(String title) {
        this.title = title;
        // Keep subject synchronized with title for consistency with base class
        super.setSubject(title);
    }
    
    /**
     * Gets the push notification body text
     *
     * @return The push notification body text
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Sets the push notification body text
     *
     * @param body The push notification body text
     */
    public void setBody(String body) {
        this.body = body;
        // Keep message synchronized with body for consistency with base class
        super.setMessage(body);
    }
    
    /**
     * Gets the badge count for the application
     *
     * @return The badge count
     */
    public Integer getBadge() {
        return badge;
    }
    
    /**
     * Sets the badge count for the application
     *
     * @param badge The badge count
     */
    public void setBadge(Integer badge) {
        // Ensure badge count is never negative
        this.badge = badge != null ? Math.max(0, badge) : 0;
    }
    
    /**
     * Gets the sound file to play for the notification
     *
     * @return The sound file
     */
    public String getSound() {
        return sound;
    }
    
    /**
     * Sets the sound file to play for the notification
     *
     * @param sound The sound file
     */
    public void setSound(String sound) {
        this.sound = sound;
    }
    
    /**
     * Gets the custom data payload for the push notification
     *
     * @return The custom data payload
     */
    public Map<String, String> getCustomData() {
        return customData;
    }
    
    /**
     * Sets the custom data payload for the push notification
     *
     * @param customData The custom data payload
     */
    public void setCustomData(Map<String, String> customData) {
        this.customData = customData != null ? customData : new HashMap<>();
    }
    
    /**
     * Adds custom data to the push notification
     *
     * @param key The key for the custom data
     * @param value The value for the custom data
     */
    public void addCustomData(String key, String value) {
        if (customData == null) {
            customData = new HashMap<>();
        }
        
        if (key != null && value != null) {
            customData.put(key, value);
        }
    }
    
    /**
     * Checks if this push notification is targeted at an iOS device
     * 
     * @return true if the platform is iOS
     */
    public boolean isIOS() {
        return "iOS".equalsIgnoreCase(platform);
    }
    
    /**
     * Checks if this push notification is targeted at an Android device
     * 
     * @return true if the platform is Android
     */
    public boolean isAndroid() {
        return "Android".equalsIgnoreCase(platform);
    }
    
    /**
     * Checks if this push notification is targeted at a Web platform
     * 
     * @return true if the platform is Web
     */
    public boolean isWeb() {
        return "Web".equalsIgnoreCase(platform);
    }
    
    /**
     * Validates if the push notification has all the required fields for sending
     * 
     * @return true if the notification is valid and can be sent
     */
    public boolean isValid() {
        return deviceToken != null && !deviceToken.isEmpty() &&
               platform != null && !platform.isEmpty() &&
               ((title != null && !title.isEmpty()) || (body != null && !body.isEmpty()));
    }
    
    /**
     * Returns a string representation of this push notification.
     *
     * @return A string representation of this push notification
     */
    @Override
    public String toString() {
        return "PushNotification{" +
                "id='" + getId() + '\'' +
                ", recipientId='" + getRecipientId() + '\'' +
                ", deviceToken='" + deviceToken + '\'' +
                ", platform='" + platform + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", badge=" + badge +
                ", status=" + getStatus() +
                '}';
    }
    
    /**
     * Compares this push notification to the specified object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PushNotification that = (PushNotification) o;
        return Objects.equals(deviceToken, that.deviceToken) &&
               Objects.equals(platform, that.platform);
    }
    
    /**
     * Returns a hash code value for this push notification.
     *
     * @return A hash code value for this push notification
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deviceToken, platform);
    }
}