package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.model.InAppNotification;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;
import com.gradlemedium200.notification.model.NotificationType;
import com.gradlemedium200.notification.model.NotificationTemplate;
import com.gradlemedium200.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Main service for orchestrating notification operations, coordinating between
 * different notification channels and managing notification lifecycle with retry logic
 * and status tracking.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_BULK_BATCH_SIZE = 100;
    private static final int DEFAULT_RETRY_LIMIT = 3;
    
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final PushNotificationService pushNotificationService;
    private final InAppNotificationService inAppNotificationService;
    private final TemplateService templateService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationRepository notificationRepository;
    private final Map<String, NotificationChannel> channelServices = new HashMap<>();
    
    // Thread pool for processing bulk notifications
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(5);

    @Autowired
    public NotificationService(
            EmailNotificationService emailNotificationService,
            SmsNotificationService smsNotificationService,
            PushNotificationService pushNotificationService,
            InAppNotificationService inAppNotificationService,
            TemplateService templateService,
            NotificationPreferenceService preferenceService,
            NotificationRepository notificationRepository) {
        this.emailNotificationService = emailNotificationService;
        this.smsNotificationService = smsNotificationService;
        this.pushNotificationService = pushNotificationService;
        this.inAppNotificationService = inAppNotificationService;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Initialize channel services map after dependency injection.
     */
    @PostConstruct
    public void init() {
        // Register all notification channels
        channelServices.put("email", emailNotificationService);
        channelServices.put("sms", smsNotificationService);
        channelServices.put("push", pushNotificationService);
        channelServices.put("in-app", inAppNotificationService);
        
        logger.info("NotificationService initialized with {} channels", channelServices.size());
    }

    /**
     * Sends a notification through appropriate channels based on user preferences.
     * 
     * @param notification The notification to send
     * @return true if notification was successfully sent to at least one channel
     */
    public boolean sendNotification(Notification notification) {
        logger.debug("Sending notification: {}", notification.getId());
        
        if (!validateAndPrepareNotification(notification)) {
            logger.warn("Notification validation failed: {}", notification.getId());
            return false;
        }
        
        // Save notification with PENDING status
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        // Determine which channels to use based on recipient preferences
        List<String> enabledChannels = determineChannels(
                notification.getRecipientId(), 
                notification.getType()
        );
        
        if (enabledChannels.isEmpty()) {
            logger.info("No enabled channels for recipient: {}", notification.getRecipientId());
            updateNotificationStatus(notification.getId(), NotificationStatus.FAILED);
            return false;
        }
        
        boolean atLeastOneSuccess = false;
        
        // Attempt to send through each enabled channel
        for (String channel : enabledChannels) {
            NotificationChannel notificationChannel = channelServices.get(channel);
            if (notificationChannel != null && notificationChannel.isEnabled()) {
                try {
                    boolean success = notificationChannel.send(notification);
                    if (success) {
                        atLeastOneSuccess = true;
                        logger.debug("Successfully sent notification {} via channel: {}", 
                                notification.getId(), channel);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send notification {} via channel {}: {}", 
                            notification.getId(), channel, e.getMessage(), e);
                }
            }
        }
        
        // Update notification status based on result
        if (atLeastOneSuccess) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            return true;
        } else {
            // If all channels failed, set to retry status if within retry limit
            if (notification.getRetryCount() < DEFAULT_RETRY_LIMIT) {
                notification.setStatus(NotificationStatus.RETRY);
                notification.incrementRetryCount();
                notificationRepository.save(notification);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
            }
            return false;
        }
    }
    
    /**
     * Sends a notification using a template with variable substitution.
     * 
     * @param recipientId The recipient ID
     * @param typeStr The notification type as a string
     * @param variables Map of variable names to values for template substitution
     * @return true if notification was successfully sent
     */
    public boolean sendTemplatedNotification(String recipientId, String typeStr, Map<String, String> variables) {
        // Convert typeStr to NotificationType
        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid notification type: {}", typeStr);
            return false;
        }
        // Get appropriate template
        Optional<NotificationTemplate> templateOpt = templateService.getTemplate(type, 
                variables.getOrDefault("language", "en"));
        
        if (!templateOpt.isPresent()) {
            logger.error("No template found for type: {} and language: {}", 
                    type, variables.getOrDefault("language", "en"));
            return false;
        }
        
        NotificationTemplate template = templateOpt.get();
        
        // Process template with variables
        String subject = templateService.processTemplate(template.getSubject(), variables);
        String message;
        if (template.getBody() != null) {
            message = templateService.processTemplate(template.getBody(), variables);
        } else {
            // Fallback to content if body isn't available
            message = templateService.processTemplate(template.getContent(), variables);
        }
        
        // Create notification
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setSubject(subject);
        notification.setMessage(message);
        
        // Send notification
        return sendNotification(notification);
    }
    
    /**
     * Sends notifications to multiple recipients.
     * 
     * @param recipientIds List of recipient IDs
     * @param type The notification type
     * @param message The notification message
     * @return List of successfully sent notification IDs
     */
    public List<String> sendBulkNotification(List<String> recipientIds, NotificationType type, String message) {
        logger.info("Sending bulk notification to {} recipients", recipientIds.size());
        
        List<String> successfulNotifications = new ArrayList<>();
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // Process in batches to avoid overloading system
        for (int i = 0; i < recipientIds.size(); i += MAX_BULK_BATCH_SIZE) {
            List<String> batch = recipientIds.subList(
                    i, Math.min(i + MAX_BULK_BATCH_SIZE, recipientIds.size()));
            
            for (String recipientId : batch) {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    Notification notification = new Notification();
                    notification.setId(UUID.randomUUID().toString());
                    notification.setRecipientId(recipientId);
                    notification.setType(type);
                    notification.setMessage(message);
                    notification.setCreatedAt(LocalDateTime.now());
                    
                    boolean success = sendNotification(notification);
                    return success ? notification.getId() : null;
                }, notificationExecutor);
                
                futures.add(future);
            }
        }
        
        // Collect results from all futures
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<String> future : futures) {
            try {
                String notificationId = future.get();
                if (notificationId != null) {
                    successfulNotifications.add(notificationId);
                }
            } catch (Exception e) {
                logger.error("Error processing bulk notification", e);
            }
        }
        
        logger.info("Bulk notification completed. Success: {}/{}", 
                successfulNotifications.size(), recipientIds.size());
        
        return successfulNotifications;
    }
    
    /**
     * Retries sending of failed notifications.
     * 
     * @return Count of notifications that were retried
     */
    public int retryFailedNotifications() {
        logger.info("Retrying failed notifications");
        
        // Get all notifications in RETRY status
        List<Notification> retryNotifications = notificationRepository.findByStatus(NotificationStatus.RETRY);
        int retryCount = 0;
        
        for (Notification notification : retryNotifications) {
            try {
                // Increment retry count
                notification.incrementRetryCount();
                
                // If exceeds maximum retries, mark as failed
                if (notification.getRetryCount() > DEFAULT_RETRY_LIMIT) {
                    notification.setStatus(NotificationStatus.FAILED);
                    notificationRepository.save(notification);
                    continue;
                }
                
                // Attempt to send again
                boolean success = sendNotification(notification);
                if (success) {
                    retryCount++;
                }
            } catch (Exception e) {
                logger.error("Error retrying notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
            }
        }
        
        logger.info("Retried {} notifications", retryCount);
        return retryCount;
    }
    
    /**
     * Gets the current status of a notification.
     * 
     * @param notificationId The notification ID
     * @return Optional containing the notification status if found
     */
    public Optional<NotificationStatus> getNotificationStatus(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        return notification.map(Notification::getStatus);
    }
    
    /**
     * Gets notification history for a recipient.
     * 
     * @param recipientId The recipient ID
     * @return List of notifications for the recipient
     */
    public List<Notification> getNotificationHistory(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }
    
    /**
     * Processes pending notifications in the queue.
     * This method is scheduled to run at a fixed interval.
     */
    @Scheduled(fixedDelay = 60000) // Run every 1 minute
    public void processNotificationQueue() {
        logger.debug("Processing notification queue");
        
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications();
        
        logger.debug("Found {} pending notifications", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notification);
            } catch (Exception e) {
                logger.error("Error processing queued notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Validates and prepares a notification for sending.
     * 
     * @param notification The notification to validate
     * @return true if notification is valid and ready to send
     */
    public boolean validateAndPrepareNotification(Notification notification) {
        // Check required fields
        if (notification == null || notification.getRecipientId() == null || 
                notification.getMessage() == null) {
            logger.warn("Notification missing required fields");
            return false;
        }
        
        // Ensure notification has an ID
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID().toString());
        }
        
        // Ensure notification has a type
        if (notification.getType() == null) {
            notification.setType(NotificationType.SYSTEM_ALERT);
        }
        
        // If notification doesn't have a subject, create one based on type
        if (notification.getSubject() == null) {
            switch (notification.getType()) {
                case ORDER_CONFIRMATION:
                    notification.setSubject("Order Confirmation");
                    break;
                case SHIPPING_UPDATE:
                    notification.setSubject("Shipping Update");
                    break;
                case PROMOTIONAL:
                    notification.setSubject("Special Offer");
                    break;
                case USER_REGISTRATION:
                    notification.setSubject("Welcome to Our Service");
                    break;
                case PASSWORD_RESET:
                    notification.setSubject("Password Reset Request");
                    break;
                default:
                    notification.setSubject("System Notification");
                    break;
            }
        }
        
        return true;
    }
    
    /**
     * Determines which channels to use for notification delivery based on user preferences.
     * 
     * @param recipientId The recipient ID
     * @param type The notification type
     * @return List of channel names to use for delivery
     */
    public List<String> determineChannels(String recipientId, NotificationType type) {
        // Get enabled channels from user preferences
        List<String> enabledChannels = preferenceService.getEnabledChannels(recipientId, type);
        
        // If user has no specific preferences or all channels disabled, use default channel (in-app)
        if (enabledChannels.isEmpty()) {
            logger.debug("No enabled channels for user {}, using default in-app channel", recipientId);
            return Collections.singletonList("in-app");
        }
        
        // Check if currently in quiet hours
        if (preferenceService.isWithinQuietHours(recipientId)) {
            logger.debug("Within quiet hours for user {}, using only in-app notification", recipientId);
            // During quiet hours, only use in-app notifications if enabled
            if (enabledChannels.contains("in-app")) {
                return Collections.singletonList("in-app");
            } else {
                // If in-app not enabled during quiet hours, queue for later delivery
                return Collections.emptyList();
            }
        }
        
        // For critical notifications (SYSTEM_ALERT), use all available channels regardless of preferences
        if (type == NotificationType.SYSTEM_ALERT) {
            return enabledChannels.stream()
                    .filter(channel -> channelServices.containsKey(channel) && 
                            channelServices.get(channel).isEnabled())
                    .collect(Collectors.toList());
        }
        
        // Filter enabled channels to only include those that are actually available
        return enabledChannels.stream()
                .filter(channel -> {
                    return channelServices.containsKey(channel) && 
                           channelServices.get(channel).isEnabled() && 
                           preferenceService.shouldSendNotification(recipientId, type, channel);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Determines which channels to use for notification delivery based on user preferences.
     * 
     * @param recipientId The recipient ID
     * @param typeStr The notification type as a string
     * @return List of channel names to use for delivery
     */
    public List<String> determineChannels(String recipientId, String typeStr) {
        try {
            NotificationType type = NotificationType.valueOf(typeStr);
            return determineChannels(recipientId, type);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid notification type: {}", typeStr);
            return Collections.singletonList("in-app"); // Default to in-app notification
        }
    }
    
    /**
     * Gets a notification by its ID.
     * 
     * @param notificationId The notification ID
     * @return Optional containing the notification if found
     */
    public Optional<Notification> getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }
    
    /**
     * Gets notification history for a recipient with pagination.
     * 
     * @param recipientId The recipient ID
     * @param page The page number (0-based)
     * @param size The page size
     * @return List of notifications for the recipient
     */
    public List<Notification> getNotificationHistoryPaginated(String recipientId, int page, int size) {
        List<Notification> allNotifications = notificationRepository.findByRecipientId(recipientId);
        
        // Manual pagination since our repository implementation doesn't support it
        int start = page * size;
        if (start >= allNotifications.size()) {
            return Collections.emptyList();
        }
        
        int end = Math.min(start + size, allNotifications.size());
        return allNotifications.subList(start, end);
    }
    
    /**
     * Gets unread in-app notifications for a user.
     * 
     * @param userId The user ID
     * @return List of unread in-app notifications
     */
    public List<InAppNotification> getUnreadInAppNotifications(String userId) {
        return inAppNotificationService.getUnreadNotifications(userId);
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId The notification ID
     * @return true if successfully marked as read, false if notification not found
     */
    public boolean markNotificationAsRead(String notificationId) {
        return inAppNotificationService.markAsRead(notificationId);
    }
    
    /**
     * Updates the status of a notification.
     * 
     * @param notificationId The notification ID
     * @param status The new notification status
     */
    public void updateNotificationStatus(String notificationId, NotificationStatus status) {
        logger.debug("Updating notification {} status to {}", notificationId, status);
        notificationRepository.updateStatus(notificationId, status);
        
        // If notification is delivered, record metrics
        if (status == NotificationStatus.DELIVERED) {
            // TODO: Record delivery metrics for reporting and analytics
        }
    }
}