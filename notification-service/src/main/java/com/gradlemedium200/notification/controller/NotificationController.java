package com.gradlemedium200.notification.controller;

import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationPreference;
import com.gradlemedium200.notification.model.NotificationTemplate;
import com.gradlemedium200.notification.model.BulkNotificationRequest;
import com.gradlemedium200.notification.model.InAppNotification;
import com.gradlemedium200.notification.model.NotificationType;
import com.gradlemedium200.notification.service.NotificationService;
import com.gradlemedium200.notification.service.TemplateService;
import com.gradlemedium200.notification.service.NotificationPreferenceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * REST controller for handling notification-related HTTP requests.
 * Provides API endpoints for sending notifications, managing templates,
 * and handling notification preferences.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = Logger.getLogger(NotificationController.class.getName());
    
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final NotificationPreferenceService preferenceService;

    /**
     * Constructor with required service dependencies.
     * 
     * @param notificationService The notification service
     * @param templateService The template service
     * @param preferenceService The preference service
     */
    @Autowired
    public NotificationController(NotificationService notificationService, 
                                  TemplateService templateService,
                                  NotificationPreferenceService preferenceService) {
        this.notificationService = notificationService;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
    }

    /**
     * Sends a single notification.
     * 
     * @param notification The notification to send
     * @return Response containing status and notification ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Notification notification) {
        logger.info("Received request to send notification to: " + notification.getRecipientId());
        
        boolean result = notificationService.sendNotification(notification);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("notificationId", notification.getId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a templated notification with dynamic variables.
     * 
     * @param recipientId The ID of the recipient
     * @param type The notification type
     * @param variables The variables for template substitution
     * @return Response containing status and notification ID
     */
    @PostMapping("/templated")
    public ResponseEntity<Map<String, Object>> sendTemplatedNotification(
            @RequestParam String recipientId,
            @RequestParam String type,
            @RequestBody Map<String, String> variables) {
        
        logger.info("Received request to send templated notification to: " + recipientId);
        
        try {
            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            boolean result = notificationService.sendTemplatedNotification(recipientId, type.toUpperCase(), variables);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("recipientId", recipientId);
            response.put("type", type);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Invalid notification type
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Invalid notification type: " + type);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Sends notifications to multiple recipients.
     * 
     * @param request The bulk notification request
     * @return Response containing status and count of sent notifications
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkNotification(@RequestBody BulkNotificationRequest request) {
        logger.info("Received request to send bulk notification to " + request.getRecipientIds().size() + " recipients");
        
        List<String> sentNotificationIds;
        
        if (request.isUseTemplate()) {
            // Use templated notification for bulk sending
            sentNotificationIds = notificationService.sendBulkNotification(
                    request.getRecipientIds(), 
                    request.getType(), 
                    request.getMessage());
        } else {
            // Create individual notifications for each recipient
            Notification baseNotification = new Notification();
            baseNotification.setMessage(request.getMessage());
            baseNotification.setSubject(request.getSubject());
            baseNotification.setType(request.getType());
            
            sentNotificationIds = notificationService.sendBulkNotification(
                    request.getRecipientIds(), 
                    request.getType(), 
                    request.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sentCount", sentNotificationIds.size());
        response.put("notificationIds", sentNotificationIds);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the status of a specific notification.
     * 
     * @param notificationId The notification ID
     * @return Response containing notification status
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> getNotificationStatus(@PathVariable String notificationId) {
        logger.info("Received request for notification status: " + notificationId);
        
        Optional<Notification> notificationOpt = notificationService.getNotificationById(notificationId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            response.put("id", notification.getId());
            response.put("status", notification.getStatus());
            response.put("recipientId", notification.getRecipientId());
            response.put("createdAt", notification.getCreatedAt());
            response.put("sentAt", notification.getSentAt());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Notification not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Gets paginated notification history for a recipient.
     * 
     * @param recipientId The recipient ID
     * @param page The page number (0-based)
     * @param size The page size
     * @return Response containing a list of notifications
     */
    @GetMapping("/history")
    public ResponseEntity<List<Notification>> getNotificationHistory(
            @RequestParam String recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Received request for notification history for recipient: " + recipientId);
        
        // Get notifications with pagination
        List<Notification> notifications = notificationService.getNotificationHistoryPaginated(
                recipientId, page, size);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Gets notification preferences for a user.
     * 
     * @param userId The user ID
     * @return Response containing a list of notification preferences
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<List<NotificationPreference>> getUserPreferences(@PathVariable String userId) {
        logger.info("Received request for user preferences: " + userId);
        
        List<NotificationPreference> preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Updates notification preferences for a user.
     * 
     * @param userId The user ID
     * @param preference The updated preference
     * @return Response containing the updated preference
     */
    @PutMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreference> updateUserPreferences(
            @PathVariable String userId,
            @RequestBody NotificationPreference preference) {
        
        logger.info("Received request to update preferences for user: " + userId);
        
        // Ensure the user ID in the preference matches the path variable
        preference.setUserId(userId);
        
        NotificationPreference updatedPreference = preferenceService.updatePreference(preference);
        return ResponseEntity.ok(updatedPreference);
    }

    /**
     * Gets notification templates by type.
     * 
     * @param type The template type (optional)
     * @return Response containing a list of templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> getTemplates(
            @RequestParam(required = false) String type) {
        
        List<NotificationTemplate> templates;
        
        if (type != null && !type.isEmpty()) {
            try {
                NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
                templates = templateService.getTemplatesByType(notificationType);
                logger.info("Fetched " + templates.size() + " templates for type: " + type);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            // FIXME: Need to implement a method to get all templates
            templates = templateService.getAllTemplates();
            logger.info("Fetched all " + templates.size() + " templates");
        }
        
        return ResponseEntity.ok(templates);
    }

    /**
     * Creates a new notification template.
     * 
     * @param template The template to create
     * @return Response containing the created template
     */
    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplate template) {
        logger.info("Received request to create template: " + template.getName());
        
        if (!templateService.validateTemplate(template)) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationTemplate createdTemplate = templateService.createTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    /**
     * Updates an existing notification template.
     * 
     * @param templateId The template ID
     * @param template The updated template
     * @return Response containing the updated template
     */
    @PutMapping("/templates/{templateId}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable String templateId,
            @RequestBody NotificationTemplate template) {
        
        logger.info("Received request to update template: " + templateId);
        
        // Ensure the template ID matches the path variable
        template.setId(templateId);
        
        if (!templateService.validateTemplate(template)) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationTemplate updatedTemplate = templateService.updateTemplate(template);
        
        if (updatedTemplate != null) {
            return ResponseEntity.ok(updatedTemplate);
        } else {
            // Template not found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a notification template.
     * 
     * @param templateId The template ID
     * @return Empty response with appropriate status
     */
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateId) {
        logger.info("Received request to delete template: " + templateId);
        
        try {
            templateService.deleteTemplate(templateId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.warning("Error deleting template: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Triggers retry of failed notifications.
     * 
     * @return Response containing count of retried notifications
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, Object>> retryFailedNotifications() {
        logger.info("Received request to retry failed notifications");
        
        int retriedCount = notificationService.retryFailedNotifications();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("retriedCount", retriedCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets unread in-app notifications for a user.
     * 
     * @param userId The user ID
     * @return Response containing a list of unread notifications
     */
    @GetMapping("/inapp/{userId}")
    public ResponseEntity<List<InAppNotification>> getUnreadInAppNotifications(@PathVariable String userId) {
        logger.info("Received request for unread in-app notifications for user: " + userId);
        
        // Assume the notification service has a method to get unread notifications
        List<InAppNotification> notifications = notificationService.getUnreadInAppNotifications(userId);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks an in-app notification as read.
     * 
     * @param notificationId The notification ID
     * @return Empty response with appropriate status
     */
    @PostMapping("/inapp/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId) {
        logger.info("Received request to mark notification as read: " + notificationId);
        
        // Assume the notification service has a method to mark a notification as read
        boolean success = notificationService.markNotificationAsRead(notificationId);
        
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}