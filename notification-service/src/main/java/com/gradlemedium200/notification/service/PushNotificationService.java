package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.aws.SnsPublisher;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.PushNotification;
import com.gradlemedium200.notification.model.NotificationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling push notification delivery to mobile devices through AWS SNS
 * with platform-specific formatting and device token management.
 * 
 * <p>This service implements the NotificationChannel interface to handle push notifications
 * for mobile devices on different platforms (iOS, Android) with specific formatting requirements.</p>
 * 
 * @author gradlemedium200
 */
@Service
public class PushNotificationService implements NotificationChannel {
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    
    // Regular expressions for validating device tokens
    private static final Pattern IOS_TOKEN_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");
    private static final Pattern ANDROID_TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    
    /**
     * SNS publisher for sending push notifications
     */
    private final SnsPublisher snsPublisher;
    
    /**
     * ARN of the iOS platform application
     */
    @Value("${aws.sns.ios.application.arn}")
    private String iosApplicationArn;
    
    /**
     * ARN of the Android platform application
     */
    @Value("${aws.sns.android.application.arn}")
    private String androidApplicationArn;
    
    /**
     * Maximum retry attempts for push notification delivery
     */
    @Value("${notification.push.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    /**
     * Flag indicating if push notification channel is enabled
     */
    @Value("${notification.push.enabled:true}")
    private boolean enabled;
    
    /**
     * Constructor for the PushNotificationService.
     * 
     * @param snsPublisher The SNS publisher for sending notifications
     */
    @Autowired
    public PushNotificationService(SnsPublisher snsPublisher) {
        this.snsPublisher = snsPublisher;
    }
    
    /**
     * Sends a push notification through SNS.
     * 
     * <p>This method validates the notification, converts it to a PushNotification if needed,
     * and formats the message appropriately for the target platform before sending.</p>
     * 
     * @param notification The notification to send
     * @return true if the notification was sent successfully, false otherwise
     */
    @Override
    public boolean send(Notification notification) {
        if (!isEnabled()) {
            logger.warn("Push notification channel is disabled. Skipping notification: {}", notification.getId());
            return false;
        }
        
        if (!validate(notification)) {
            logger.error("Invalid push notification: {}", notification.getId());
            return false;
        }
        
        PushNotification pushNotification;
        if (notification instanceof PushNotification) {
            pushNotification = (PushNotification) notification;
        } else {
            // This should not happen as validate() should have checked this
            logger.error("Notification is not a PushNotification instance: {}", notification.getId());
            return false;
        }
        
        return sendPushNotification(pushNotification);
    }
    
    /**
     * Validates push notification before sending.
     * 
     * <p>Checks that the notification is a PushNotification instance and has
     * all required fields for successful delivery.</p>
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
        
        if (!(notification instanceof PushNotification)) {
            logger.error("Notification is not a PushNotification instance");
            return false;
        }
        
        PushNotification pushNotification = (PushNotification) notification;
        
        if (pushNotification.getDeviceToken() == null || pushNotification.getDeviceToken().isEmpty()) {
            logger.error("Device token is missing");
            return false;
        }
        
        if (pushNotification.getPlatform() == null || pushNotification.getPlatform().isEmpty()) {
            logger.error("Platform is missing");
            return false;
        }
        
        // Platform-specific token validation
        if (!validateDeviceToken(pushNotification.getDeviceToken(), pushNotification.getPlatform())) {
            logger.error("Invalid device token format for platform: {}", pushNotification.getPlatform());
            return false;
        }
        
        // At least one of title or body should be present
        if ((pushNotification.getTitle() == null || pushNotification.getTitle().isEmpty()) && 
            (pushNotification.getBody() == null || pushNotification.getBody().isEmpty())) {
            logger.error("Both title and body are empty");
            return false;
        }
        
        return true;
    }
    
    /**
     * Sends a specific push notification.
     * 
     * <p>Formats the notification payload according to the target platform
     * and sends it through SNS.</p>
     * 
     * @param pushNotification The push notification to send
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendPushNotification(PushNotification pushNotification) {
        logger.info("Sending push notification to device on platform: {}", pushNotification.getPlatform());
        
        try {
            String platformArn = getPlatformApplicationArn(pushNotification.getPlatform());
            if (platformArn == null) {
                logger.error("Unknown platform: {}", pushNotification.getPlatform());
                pushNotification.setStatus(NotificationStatus.FAILED);
                return false;
            }
            
            // Format message for the specific platform
            String formattedMessage = formatForPlatform(pushNotification);
            
            // Send the notification through SNS
            String messageId = snsPublisher.publishPushMessage(
                    formattedMessage, 
                    platformArn, 
                    pushNotification.getDeviceToken());
            
            if (messageId != null && !messageId.isEmpty()) {
                pushNotification.setStatus(NotificationStatus.SENT);
                logger.info("Push notification sent successfully. Message ID: {}", messageId);
                return true;
            } else {
                pushNotification.setStatus(NotificationStatus.FAILED);
                logger.error("Failed to send push notification - no message ID returned");
                return false;
            }
        } catch (Exception e) {
            pushNotification.setStatus(NotificationStatus.FAILED);
            logger.error("Error sending push notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Formats push notification message for specific platform.
     * 
     * <p>Creates a platform-specific JSON payload that conforms to the requirements
     * of each mobile platform (iOS, Android).</p>
     * 
     * @param pushNotification The push notification to format
     * @return A properly formatted message string for the target platform
     */
    public String formatForPlatform(PushNotification pushNotification) {
        if (pushNotification.isIOS()) {
            return createIOSPayload(pushNotification);
        } else if (pushNotification.isAndroid()) {
            return createAndroidPayload(pushNotification);
        } else {
            // Default format for other platforms
            JSONObject payload = new JSONObject();
            payload.put("title", pushNotification.getTitle());
            payload.put("body", pushNotification.getBody());
            
            if (pushNotification.getCustomData() != null && !pushNotification.getCustomData().isEmpty()) {
                JSONObject data = new JSONObject();
                for (Map.Entry<String, String> entry : pushNotification.getCustomData().entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
                payload.put("data", data);
            }
            
            return payload.toString();
        }
    }
    
    /**
     * Validates device token format for the platform.
     * 
     * <p>Ensures that device tokens conform to the expected format for each platform.</p>
     * 
     * @param deviceToken The device token to validate
     * @param platform The platform (iOS, Android)
     * @return true if the device token is valid for the platform, false otherwise
     */
    public boolean validateDeviceToken(String deviceToken, String platform) {
        if (deviceToken == null || platform == null) {
            return false;
        }
        
        if ("iOS".equalsIgnoreCase(platform)) {
            // iOS tokens are 64 character hex strings
            return IOS_TOKEN_PATTERN.matcher(deviceToken).matches();
        } else if ("Android".equalsIgnoreCase(platform)) {
            // FCM tokens can contain alphanumeric characters, underscores and hyphens
            // and are typically longer than a minimum length
            return ANDROID_TOKEN_PATTERN.matcher(deviceToken).matches() && deviceToken.length() >= 20;
        }
        
        // For unknown platforms, just check that the token isn't empty
        return !deviceToken.trim().isEmpty();
    }
    
    /**
     * Gets the platform application ARN for the specified platform.
     * 
     * @param platform The platform name (iOS, Android)
     * @return The ARN for the platform application, or null if unknown platform
     */
    public String getPlatformApplicationArn(String platform) {
        if ("iOS".equalsIgnoreCase(platform)) {
            return iosApplicationArn;
        } else if ("Android".equalsIgnoreCase(platform)) {
            return androidApplicationArn;
        }
        
        logger.warn("Unknown platform: {}. No ARN available.", platform);
        return null;
    }
    
    /**
     * Creates iOS-specific push notification payload.
     * 
     * <p>Formats notification payload according to Apple Push Notification Service (APNS) 
     * specifications.</p>
     * 
     * @param pushNotification The push notification to format
     * @return A properly formatted APNS message payload as a JSON string
     */
    public String createIOSPayload(PushNotification pushNotification) {
        // Create APNS payload structure
        JSONObject aps = new JSONObject();
        
        // Alert can be a string or an object with title and body
        JSONObject alert = new JSONObject();
        if (pushNotification.getTitle() != null && !pushNotification.getTitle().isEmpty()) {
            alert.put("title", pushNotification.getTitle());
        }
        if (pushNotification.getBody() != null && !pushNotification.getBody().isEmpty()) {
            alert.put("body", pushNotification.getBody());
        }
        aps.put("alert", alert);
        
        // Add badge number if present
        if (pushNotification.getBadge() != null) {
            aps.put("badge", pushNotification.getBadge());
        }
        
        // Add sound if present
        if (pushNotification.getSound() != null && !pushNotification.getSound().isEmpty()) {
            aps.put("sound", pushNotification.getSound());
        }
        
        // Root payload contains 'aps' and any custom data
        JSONObject payload = new JSONObject();
        payload.put("aps", aps);
        
        // Add custom data if present
        if (pushNotification.getCustomData() != null && !pushNotification.getCustomData().isEmpty()) {
            for (Map.Entry<String, String> entry : pushNotification.getCustomData().entrySet()) {
                // Don't overwrite the aps dictionary with custom data
                if (!entry.getKey().equals("aps")) {
                    payload.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // For SNS, we need to wrap this in a message format
        JSONObject message = new JSONObject();
        message.put("default", pushNotification.getBody() != null ? 
                pushNotification.getBody() : pushNotification.getTitle());
        message.put("APNS", payload.toString());
        message.put("APNS_SANDBOX", payload.toString()); // Also add for sandbox environment
        
        return message.toString();
    }
    
    /**
     * Creates Android-specific push notification payload.
     * 
     * <p>Formats notification payload according to Firebase Cloud Messaging (FCM) 
     * specifications.</p>
     * 
     * @param pushNotification The push notification to format
     * @return A properly formatted FCM message payload as a JSON string
     */
    public String createAndroidPayload(PushNotification pushNotification) {
        // Create FCM payload structure
        JSONObject notification = new JSONObject();
        if (pushNotification.getTitle() != null && !pushNotification.getTitle().isEmpty()) {
            notification.put("title", pushNotification.getTitle());
        }
        if (pushNotification.getBody() != null && !pushNotification.getBody().isEmpty()) {
            notification.put("body", pushNotification.getBody());
        }
        
        // Sound for Android
        if (pushNotification.getSound() != null && !pushNotification.getSound().isEmpty()) {
            notification.put("sound", pushNotification.getSound());
        }
        
        // Root payload structure
        JSONObject payload = new JSONObject();
        payload.put("notification", notification);
        
        // Add custom data
        if (pushNotification.getCustomData() != null && !pushNotification.getCustomData().isEmpty()) {
            JSONObject data = new JSONObject();
            for (Map.Entry<String, String> entry : pushNotification.getCustomData().entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
            payload.put("data", data);
        }
        
        // Priority can be "normal" or "high"
        payload.put("priority", "high");
        
        // For SNS, we need to wrap this in a message format
        JSONObject message = new JSONObject();
        message.put("default", pushNotification.getBody() != null ? 
                pushNotification.getBody() : pushNotification.getTitle());
        message.put("GCM", payload.toString());
        
        return message.toString();
    }
    
    /**
     * Returns the name of the push notification channel.
     * 
     * @return The channel name ("push")
     */
    @Override
    public String getChannelName() {
        return "push";
    }
    
    /**
     * Checks if push notification channel is enabled.
     * 
     * @return true if this channel is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Returns the maximum number of retry attempts for this channel.
     * 
     * @return The maximum retry attempts
     */
    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    /**
     * Returns the priority level for the push notification channel.
     * Push notifications have a higher priority than some other channels.
     * 
     * @return The priority level (2)
     */
    @Override
    public int getPriority() {
        // Push notifications have higher priority than email but lower than SMS
        return 2;
    }
}