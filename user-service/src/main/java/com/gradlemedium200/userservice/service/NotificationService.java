package com.gradlemedium200.userservice.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.gradlemedium200.userservice.config.AwsSnsConfig;
import com.gradlemedium200.userservice.config.AwsSqsConfig;
import com.gradlemedium200.userservice.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for managing user notifications and messaging 
 * through AWS SQS and SNS integration.
 * 
 * This service handles direct notification delivery and queueing for later processing.
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_PROCESSING_ATTEMPTS = 3;
    private static final int NOTIFICATION_BATCH_SIZE = 10;
    
    private final AwsSqsConfig sqsConfig;
    private final AwsSnsConfig snsConfig;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public NotificationService(AwsSqsConfig sqsConfig, AwsSnsConfig snsConfig, ObjectMapper objectMapper) {
        this.sqsConfig = sqsConfig;
        this.snsConfig = snsConfig;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Sends a notification directly to the user via SNS.
     * 
     * @param notification The notification to be sent
     */
    public void sendNotification(Notification notification) {
        try {
            validateNotification(notification);
            
            AmazonSNS snsClient = snsConfig.getAmazonSNSClient();
            String topicArn = determineTargetTopic(notification);
            
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(notificationJson)
                .withSubject(notification.getSubject());
            
            // Add SMS attributes if the notification is for SMS
            if (notification.getType() != null && notification.getType().equalsIgnoreCase("SMS")) {
                publishRequest.addMessageAttributesEntry("AWS.SNS.SMS.SenderID", 
                    new com.amazonaws.services.sns.model.MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(snsConfig.getSmsSenderId()));
            }
            
            PublishResult result = snsClient.publish(publishRequest);
            
            logger.info("Notification sent successfully. MessageId: {}", result.getMessageId());
            notification.setStatus("SENT");
            notification.setMessageId(result.getMessageId());
            
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            // FIXME: Consider implementing a retry mechanism for failed notifications
        }
    }
    
    /**
     * Queues a notification for later processing using SQS.
     * 
     * @param notification The notification to be queued
     */
    public void queueNotification(Notification notification) {
        try {
            validateNotification(notification);
            
            AmazonSQS sqsClient = sqsConfig.getAmazonSQSClient();
            String queueUrl = sqsConfig.getNotificationQueueUrl();
            
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(notificationJson)
                .withDelaySeconds(calculateDelay(notification));
            
            // Add attributes for filtering if needed
            if (notification.getPriority() != null) {
                sendMessageRequest.addMessageAttributesEntry("Priority", 
                    new com.amazonaws.services.sqs.model.MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(notification.getPriority().toString()));
            }
            
            SendMessageResult result = sqsClient.sendMessage(sendMessageRequest);
            
            logger.info("Notification queued successfully. MessageId: {}", result.getMessageId());
            notification.setStatus("QUEUED");
            notification.setMessageId(result.getMessageId());
            
        } catch (Exception e) {
            logger.error("Failed to queue notification: {}", e.getMessage(), e);
            notification.setStatus("QUEUE_FAILED");
            notification.setErrorMessage(e.getMessage());
            // TODO: Implement fallback mechanism for queue failures
        }
    }
    
    /**
     * Processes the notification queue at regular intervals.
     * This method is scheduled to run at a fixed rate.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processNotificationQueue() {
        logger.debug("Starting notification queue processing");
        AmazonSQS sqsClient = sqsConfig.getAmazonSQSClient();
        String queueUrl = sqsConfig.getNotificationQueueUrl();
        
        try {
            // Request to receive messages from the queue
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(NOTIFICATION_BATCH_SIZE)
                .withVisibilityTimeout(30) // 30 seconds visibility timeout
                .withWaitTimeSeconds(5); // 5 seconds of long polling
                
            ReceiveMessageResult result = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = result.getMessages();
            
            if (messages.isEmpty()) {
                logger.debug("No notifications in queue to process");
                return;
            }
            
            logger.info("Processing {} notifications from queue", messages.size());
            
            for (Message message : messages) {
                try {
                    // Parse the notification from the message body
                    Notification notification = objectMapper.readValue(message.getBody(), Notification.class);
                    
                    // Process the notification
                    processQueuedNotification(notification);
                    
                    // Delete the message from the queue after successful processing
                    DeleteMessageRequest deleteRequest = new DeleteMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withReceiptHandle(message.getReceiptHandle());
                    
                    sqsClient.deleteMessage(deleteRequest);
                    logger.debug("Message deleted from queue: {}", message.getMessageId());
                    
                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", message.getMessageId(), e.getMessage(), e);
                    // TODO: Implement dead-letter queue handling for failed messages
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in queue processing: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process a single queued notification.
     * 
     * @param notification The notification to process
     */
    private void processQueuedNotification(Notification notification) {
        // Check notification attempts
        Integer attempts = notification.getAttempts();
        if (attempts == null) {
            attempts = 0;
        }
        
        if (attempts >= MAX_PROCESSING_ATTEMPTS) {
            logger.warn("Notification exceeded max processing attempts: {}", notification.getId());
            notification.setStatus("PROCESSING_FAILED");
            // TODO: Save failed notification to database for audit purposes
            return;
        }
        
        // Increment attempts
        notification.setAttempts(attempts + 1);
        
        try {
            // Send the actual notification
            sendNotification(notification);
        } catch (Exception e) {
            logger.error("Failed to process queued notification: {}", e.getMessage(), e);
            notification.setStatus("PROCESSING_ERROR");
            notification.setErrorMessage(e.getMessage());
            
            // If still under the max attempts, requeue with delay
            if (attempts + 1 < MAX_PROCESSING_ATTEMPTS) {
                try {
                    // Exponential backoff for retries
                    int delaySeconds = (int) Math.pow(2, attempts);
                    TimeUnit.SECONDS.sleep(delaySeconds);
                    queueNotification(notification);
                } catch (Exception ex) {
                    logger.error("Failed to requeue notification: {}", ex.getMessage(), ex);
                }
            }
        }
    }
    
    /**
     * Validates that the notification has all required fields.
     * 
     * @param notification The notification to validate
     * @throws IllegalArgumentException if the notification is invalid
     */
    private void validateNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        
        if (notification.getUserId() == null) {
            throw new IllegalArgumentException("Notification must have a user ID");
        }
        
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification must have a message");
        }
    }
    
    /**
     * Determines the target SNS topic ARN based on notification type.
     * 
     * @param notification The notification to analyze
     * @return The appropriate SNS topic ARN
     */
    private String determineTargetTopic(Notification notification) {
        String type = notification.getType();
        if (type == null) {
            return snsConfig.getDefaultTopicArn();
        }
        
        switch (type.toUpperCase()) {
            case "EMAIL":
                return snsConfig.getEmailTopicArn();
            case "SMS":
                return snsConfig.getSmsTopicArn();
            case "PUSH":
                return snsConfig.getPushNotificationTopicArn();
            default:
                return snsConfig.getDefaultTopicArn();
        }
    }
    
    /**
     * Calculates the delay for the notification based on priority.
     * 
     * @param notification The notification to analyze
     * @return The delay in seconds
     */
    private int calculateDelay(Notification notification) {
        if (notification.getPriority() == null) {
            return 0;
        }
        
        switch (notification.getPriority()) {
            case HIGH:
                return 0; // No delay for high priority
            case MEDIUM:
                return 60; // 1 minute delay for medium priority
            case LOW:
                return 300; // 5 minutes delay for low priority
            default:
                return 0;
        }
    }
}