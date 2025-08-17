package com.gradlemedium200.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gradlemedium200.client.NotificationServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Root module implementation of NotificationService that delegates to the NotificationServiceClient.
 * This service serves as a facade for the actual NotificationService in the notification-service module.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationServiceClient notificationServiceClient;

    /**
     * Constructor with required dependencies.
     *
     * @param notificationServiceClient Client for communicating with the notification service
     */
    @Autowired
    public NotificationService(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    /**
     * Sends a system alert notification.
     *
     * @param title The alert title
     * @param message The alert message
     */
    public void sendSystemAlert(String title, String message) {
        logger.info("Sending system alert: {}", title);
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "SYSTEM_ALERT");
            notificationData.put("title", title);
            notificationData.put("message", message);
            notificationData.put("priority", "HIGH");
            
            notificationServiceClient.sendNotification(notificationData);
            logger.debug("System alert sent successfully: {}", title);
        } catch (Exception e) {
            // Just log the error, don't propagate exception to avoid cascading failures
            logger.error("Failed to send system alert: {}", e.getMessage(), e);
        }
    }
}