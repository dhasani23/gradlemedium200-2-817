package com.gradlemedium200.orderservice.integration;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.gradlemedium200.orderservice.dto.NotificationDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for AWS SNS integration for sending notifications.
 * This class handles publishing messages to SNS topics.
 */
@Component
public class SnsClient {

    private static final Logger logger = LoggerFactory.getLogger(SnsClient.class);
    
    private final AmazonSNS snsClient;
    private final String topicArn;
    
    /**
     * Constructor with dependencies injected by Spring.
     * 
     * @param snsClient the AWS SNS client
     * @param topicArn the SNS topic ARN from configuration
     */
    @Autowired
    public SnsClient(AmazonSNS snsClient, @Value("${aws.sns.topic-arn}") String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
        logger.info("Initialized SNS client with topic ARN: {}", topicArn);
    }
    
    /**
     * Publishes a notification to the configured SNS topic.
     * 
     * @param notification the notification DTO containing message details
     */
    public void publishNotification(NotificationDto notification) {
        try {
            // Create message attributes from the notification object
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            
            // Add notification type as an attribute for message filtering
            messageAttributes.put("notificationType", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(notification.getNotificationType()));
            
            // Add customer ID if available
            if (notification.getCustomerId() != null) {
                messageAttributes.put("customerId", 
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(notification.getCustomerId()));
            }
            
            // Add order ID if available
            if (notification.getOrderId() != null) {
                messageAttributes.put("orderId", 
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(notification.getOrderId()));
            }
            
            // Add any custom attributes from the notification DTO
            notification.getAttributes().forEach((key, value) -> 
                messageAttributes.put(key, 
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(value)));
            
            // Build the publish request
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(notification.getMessage())
                .withSubject(notification.getSubject())
                .withMessageAttributes(messageAttributes);
            
            // Publish to SNS
            PublishResult result = snsClient.publish(publishRequest);
            logger.info("Successfully published notification to SNS: {}, message ID: {}", 
                        notification.getNotificationType(), result.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to publish notification to SNS: {}", e.getMessage(), e);
            // Depending on requirements, we might want to rethrow or handle the error differently
            throw new RuntimeException("Failed to publish notification to SNS", e);
        }
    }
    
    /**
     * Publishes an order event to the SNS topic.
     * 
     * @param orderId the ID of the order
     * @param eventType the type of event (e.g., CREATED, UPDATED, SHIPPED)
     * @param customerId the ID of the customer
     */
    public void publishOrderEvent(String orderId, String eventType, String customerId) {
        try {
            // Create subject based on event type
            String subject = "Order " + eventType.toLowerCase();
            
            // Create message content
            String message = String.format("Order event: %s for order %s", eventType, orderId);
            
            // Prepare notification DTO
            NotificationDto notification = new NotificationDto(
                "ORDER_EVENT", 
                subject, 
                message,
                orderId,
                customerId
            );
            
            // Add event type as an attribute
            notification.addAttribute("eventType", eventType);
            
            // Publish the notification
            publishNotification(notification);
            
            logger.debug("Order event published: {} for order {}", eventType, orderId);
        } catch (Exception e) {
            logger.error("Failed to publish order event to SNS: {} for order {}", eventType, orderId, e);
            throw new RuntimeException("Failed to publish order event to SNS", e);
        }
    }
    
    /**
     * Publishes a generic message to the SNS topic.
     * 
     * @param message the message content
     * @param subject the message subject
     * @return the message ID from SNS
     */
    public String publishMessage(String message, String subject) {
        try {
            // FIXME: Add validation for message and subject
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be empty");
            }
            
            // Build the publish request with minimal attributes
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message)
                .withSubject(subject != null ? subject : "Notification");
            
            // Publish to SNS and get result
            PublishResult result = snsClient.publish(publishRequest);
            String messageId = result.getMessageId();
            
            logger.info("Successfully published generic message to SNS, message ID: {}", messageId);
            
            return messageId;
        } catch (Exception e) {
            logger.error("Failed to publish generic message to SNS: {}", e.getMessage(), e);
            
            // TODO: Implement retry logic or dead-letter queue for failed messages
            
            throw new RuntimeException("Failed to publish generic message to SNS", e);
        }
    }
}