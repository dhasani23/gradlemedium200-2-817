package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.aws.SnsPublisher;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;
import com.gradlemedium200.notification.model.SmsNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Service responsible for handling SMS notification delivery through AWS SNS with 
 * phone number validation and country code support.
 * 
 * This service implements the NotificationChannel interface to handle SMS-specific
 * notification delivery logic, including phone number formatting, message truncation,
 * and retry logic.
 * 
 * @author gradlemedium200
 */
@Service
public class SmsNotificationService implements NotificationChannel {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    
    /**
     * Regex pattern for validating E.164 phone number format
     */
    private static final String PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    
    /**
     * SNS publisher for sending SMS notifications
     */
    private final SnsPublisher snsPublisher;
    
    /**
     * Default SMS sender ID
     */
    @Value("${notification.sms.sender-id:GradleMedium}")
    private String senderId;
    
    /**
     * Maximum retry attempts for SMS delivery
     */
    @Value("${notification.sms.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    /**
     * Flag indicating if SMS channel is enabled
     */
    @Value("${notification.sms.enabled:true}")
    private boolean enabled;
    
    /**
     * Maximum allowed SMS message length
     */
    @Value("${notification.sms.max-message-length:160}")
    private int maxMessageLength;
    
    @Autowired
    public SmsNotificationService(SnsPublisher snsPublisher) {
        this.snsPublisher = snsPublisher;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Sends an SMS notification through AWS SNS after validating the notification
     * and formatting the phone number. The method updates the notification status
     * based on the delivery result.
     * 
     * @param notification The notification to send
     * @return true if the notification was successfully sent, false otherwise
     */
    @Override
    public boolean send(Notification notification) {
        if (!isEnabled()) {
            logger.warn("SMS notification channel is disabled. Notification {} will not be sent", 
                    notification.getId());
            return false;
        }
        
        if (!validate(notification)) {
            logger.error("Invalid SMS notification: {}", notification);
            notification.setStatus(NotificationStatus.FAILED);
            return false;
        }
        
        if (!(notification instanceof SmsNotification)) {
            logger.error("Notification is not an SMS notification: {}", notification);
            notification.setStatus(NotificationStatus.FAILED);
            return false;
        }
        
        return sendSmsNotification((SmsNotification) notification);
    }
    
    /**
     * {@inheritDoc}
     * 
     * Validates that the notification is an instance of SmsNotification and
     * has a valid phone number and message content.
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
        
        if (!(notification instanceof SmsNotification)) {
            logger.error("Expected SmsNotification but received: {}", notification.getClass().getName());
            return false;
        }
        
        SmsNotification smsNotification = (SmsNotification) notification;
        String phoneNumber = smsNotification.getPhoneNumber();
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.error("Phone number is missing in SMS notification: {}", notification.getId());
            return false;
        }
        
        // Validate phone number format
        if (!validatePhoneNumber(phoneNumber)) {
            logger.error("Invalid phone number format: {}", phoneNumber);
            return false;
        }
        
        // Check if message content exists
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            logger.error("Message content is missing in SMS notification: {}", notification.getId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Sends a specific SMS notification using the AWS SNS service.
     * Handles formatting of the phone number, truncation of the message if needed,
     * and updating the notification status based on delivery result.
     * 
     * @param smsNotification The SMS notification to send
     * @return true if the notification was successfully sent, false otherwise
     */
    public boolean sendSmsNotification(SmsNotification smsNotification) {
        try {
            logger.debug("Sending SMS notification to: {}", smsNotification.getPhoneNumber());
            
            // Format phone number with country code
            String formattedPhoneNumber = formatPhoneNumber(
                    smsNotification.getPhoneNumber(), 
                    smsNotification.getCountryCode());
            
            // Ensure the message fits within SMS length constraints
            String truncatedMessage = truncateMessage(smsNotification.getMessage());
            
            // Set sender ID if specified in the notification, otherwise use default
            String effectiveSenderId = smsNotification.getSenderId() != null 
                    ? smsNotification.getSenderId() 
                    : this.senderId;
            
            // Publish SMS via SNS
            String messageId = snsPublisher.publishSmsMessage(truncatedMessage, formattedPhoneNumber);
            
            if (messageId != null && !messageId.isEmpty()) {
                logger.info("SMS notification sent successfully. MessageId: {}, Recipient: {}", 
                        messageId, formattedPhoneNumber);
                smsNotification.setStatus(NotificationStatus.SENT);
                return true;
            } else {
                logger.error("Failed to send SMS notification: no message ID returned");
                smsNotification.setStatus(NotificationStatus.FAILED);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error sending SMS notification: {}", e.getMessage(), e);
            smsNotification.setStatus(NotificationStatus.FAILED);
            
            // If we should retry, update status accordingly
            if (shouldRetry(smsNotification)) {
                smsNotification.incrementRetryCount();
                smsNotification.setStatus(NotificationStatus.RETRY);
            }
            
            return false;
        }
    }
    
    /**
     * Validates a phone number against the E.164 standard format.
     * Phone numbers should start with a '+' followed by the country code and 
     * national number, without spaces or special characters.
     * 
     * @param phoneNumber The phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        Matcher matcher = PHONE_PATTERN.matcher(phoneNumber);
        return matcher.matches();
    }
    
    /**
     * Formats a phone number with a country code according to E.164 format.
     * If the phone number already includes a country code (starts with '+'),
     * it is returned as is. Otherwise, the country code is prepended.
     * 
     * @param phoneNumber The phone number to format
     * @param countryCode The country code to prepend (should include the '+')
     * @return The formatted phone number
     */
    public String formatPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null) {
            return null;
        }
        
        // If phone number already has country code, return as is
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        
        // If country code is not provided, use default
        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "+1"; // Default to US
        }
        
        // Ensure country code starts with +
        if (!countryCode.startsWith("+")) {
            countryCode = "+" + countryCode;
        }
        
        // Remove leading 0 from phone number when adding country code
        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.substring(1);
        }
        
        return countryCode + phoneNumber;
    }
    
    /**
     * Truncates a message to fit within SMS length constraints.
     * If the message exceeds the maximum length, it's truncated and an
     * ellipsis is added to indicate truncation.
     * 
     * @param message The message to truncate
     * @return The truncated message
     */
    public String truncateMessage(String message) {
        if (message == null) {
            return "";
        }
        
        if (message.length() <= maxMessageLength) {
            return message;
        }
        
        // Truncate and add ellipsis to indicate truncation
        return message.substring(0, maxMessageLength - 3) + "...";
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return The name of this notification channel ("sms")
     */
    @Override
    public String getChannelName() {
        return "sms";
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return true if this channel is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return The maximum number of retry attempts for this channel
     */
    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    /**
     * {@inheritDoc}
     * 
     * SMS notifications typically have higher priority than email but lower than push
     * for time-sensitive information.
     * 
     * @return The priority level for this channel (2)
     */
    @Override
    public int getPriority() {
        return 2;
    }
}