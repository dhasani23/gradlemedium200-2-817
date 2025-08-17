package com.gradlemedium200.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gradlemedium200.config.RestTemplateConfig;

/**
 * Client for communicating with the NotificationService module.
 * Provides methods for sending notifications, retrieving history,
 * and updating notification preferences.
 * 
 * @author gradlemedium200
 */
@Component
public class NotificationServiceClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final int timeout;

    private static final String SEND_NOTIFICATION_ENDPOINT = "/api/notifications";
    private static final String SEND_BULK_NOTIFICATION_ENDPOINT = "/api/notifications/bulk";
    private static final String NOTIFICATION_HISTORY_ENDPOINT = "/api/notifications/history/{userId}";
    private static final String NOTIFICATION_PREFERENCES_ENDPOINT = "/api/notifications/preferences/{userId}";

    /**
     * Constructs a new NotificationServiceClient with the specified parameters.
     *
     * @param restTemplate The RestTemplate for HTTP communication
     * @param notificationServiceUrl The base URL for notification service
     * @param timeout The request timeout in milliseconds
     */
    @Autowired
    public NotificationServiceClient(
            RestTemplate restTemplate,
            @Value("${notification.service.url:http://localhost:8081}") String notificationServiceUrl,
            @Value("${notification.service.timeout:5000}") int timeout) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.timeout = timeout;
    }

    /**
     * Sends a notification through the notification service.
     * 
     * @param notificationData The notification data to send
     * @throws RuntimeException if there's an error communicating with the service
     */
    public void sendNotification(Object notificationData) {
        try {
            String url = notificationServiceUrl + SEND_NOTIFICATION_ENDPOINT;
            HttpEntity<Object> requestEntity = new HttpEntity<>(notificationData);
            
            // Log notification request
            logRequest("Sending notification", url, notificationData);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST,
                    requestEntity,
                    Void.class);
            
            // Log success response
            logResponse("Notification sent successfully", response.getStatusCodeValue());
        } catch (HttpStatusCodeException e) {
            // Handle HTTP errors (4xx, 5xx)
            handleHttpError("Error sending notification", e);
            throw new RuntimeException("Failed to send notification", e);
        } catch (Exception e) {
            // Handle other errors
            logError("Unexpected error sending notification", e);
            throw new RuntimeException("Failed to send notification due to unexpected error", e);
        }
    }

    /**
     * Sends bulk notifications to multiple recipients.
     * 
     * @param recipients List of recipient IDs
     * @param message The notification message to send
     * @throws RuntimeException if there's an error communicating with the service
     */
    public void sendBulkNotification(List<String> recipients, String message) {
        try {
            String url = notificationServiceUrl + SEND_BULK_NOTIFICATION_ENDPOINT;
            
            // Create bulk notification request
            Map<String, Object> bulkRequest = new HashMap<>();
            bulkRequest.put("recipientIds", recipients);
            bulkRequest.put("message", message);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bulkRequest);
            
            // Log bulk notification request
            logRequest("Sending bulk notification", url, 
                    String.format("recipients: %d, message: %s", recipients.size(), message));
            
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class);
            
            // Log success response
            logResponse("Bulk notification sent successfully", response.getStatusCodeValue());
        } catch (HttpStatusCodeException e) {
            // Handle HTTP errors
            handleHttpError("Error sending bulk notification", e);
            throw new RuntimeException("Failed to send bulk notification", e);
        } catch (Exception e) {
            // Handle other errors
            logError("Unexpected error sending bulk notification", e);
            throw new RuntimeException("Failed to send bulk notification due to unexpected error", e);
        }
    }

    /**
     * Retrieves notification history for a user with pagination support.
     * 
     * @param userId The user ID to retrieve history for
     * @param page The page number to retrieve
     * @return Notification history data
     * @throws RuntimeException if there's an error communicating with the service
     */
    public Object getNotificationHistory(String userId, int page) {
        try {
            // Build URL with path variables and query parameters
            String url = UriComponentsBuilder
                    .fromUriString(notificationServiceUrl + NOTIFICATION_HISTORY_ENDPOINT)
                    .queryParam("page", page)
                    .buildAndExpand(userId)
                    .toUriString();
            
            // Log request
            logRequest("Getting notification history", url, 
                    String.format("userId: %s, page: %d", userId, page));
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            
            // Log success
            logResponse("Retrieved notification history successfully", response.getStatusCodeValue());
            
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            // Handle HTTP errors
            handleHttpError("Error retrieving notification history", e);
            throw new RuntimeException("Failed to retrieve notification history", e);
        } catch (Exception e) {
            // Handle other errors
            logError("Unexpected error retrieving notification history", e);
            throw new RuntimeException("Failed to retrieve notification history due to unexpected error", e);
        }
    }

    /**
     * Updates notification preferences for a user.
     * 
     * @param userId The user ID to update preferences for
     * @param preferences The preference data to update
     * @throws RuntimeException if there's an error communicating with the service
     */
    public void updateNotificationPreferences(String userId, Object preferences) {
        try {
            // Build URL with path variable
            String url = notificationServiceUrl + NOTIFICATION_PREFERENCES_ENDPOINT;
            
            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("userId", userId);
            
            HttpEntity<Object> requestEntity = new HttpEntity<>(preferences);
            
            // Log request
            logRequest("Updating notification preferences", url, 
                    String.format("userId: %s, preferences: %s", userId, preferences));
            
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class,
                    urlParams);
            
            // Log success
            logResponse("Updated notification preferences successfully", response.getStatusCodeValue());
        } catch (HttpStatusCodeException e) {
            // Handle HTTP errors
            handleHttpError("Error updating notification preferences", e);
            throw new RuntimeException("Failed to update notification preferences", e);
        } catch (Exception e) {
            // Handle other errors
            logError("Unexpected error updating notification preferences", e);
            throw new RuntimeException("Failed to update notification preferences due to unexpected error", e);
        }
    }
    
    /**
     * Validates connection to the notification service and returns its availability status.
     * 
     * @return true if the notification service is available, false otherwise
     */
    public boolean isServiceAvailable() {
        try {
            String healthCheckUrl = notificationServiceUrl + "/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthCheckUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody() != null && 
                   "UP".equals(response.getBody().get("status"));
        } catch (Exception e) {
            logError("Notification service is not available", e);
            return false;
        }
    }

    // Helper methods for logging and error handling
    
    private void logRequest(String message, String url, Object payload) {
        // In a real implementation, use a proper logger
        System.out.printf("DEBUG - %s - URL: %s, Payload: %s%n", message, url, payload);
    }
    
    private void logResponse(String message, int statusCode) {
        System.out.printf("DEBUG - %s - Status code: %d%n", message, statusCode);
    }
    
    private void logError(String message, Exception e) {
        System.err.printf("ERROR - %s: %s%n", message, e.getMessage());
        // In a real implementation, use a proper logger with stack trace
    }
    
    private void handleHttpError(String message, HttpStatusCodeException e) {
        System.err.printf("ERROR - %s: HTTP %d - %s - Response: %s%n", 
                message, e.getRawStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
        // In a real implementation, use a proper logger with stack trace
    }
    
    // TODO: Add support for asynchronous notification sending
    
    // TODO: Implement circuit breaker pattern to handle service unavailability
    
    // FIXME: Need to implement proper error handling with retries for transient failures
}