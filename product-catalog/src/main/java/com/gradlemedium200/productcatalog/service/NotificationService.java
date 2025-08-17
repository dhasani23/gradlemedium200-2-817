package com.gradlemedium200.productcatalog.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.gradlemedium200.productcatalog.config.AwsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;

/**
 * Service for sending notifications via AWS SNS and SQS for product-related events.
 * Handles various notification types including product updates, inventory alerts, 
 * recommendation updates, and price changes.
 */
@Service
public class NotificationService {

    private final AmazonSNS amazonSNS;
    private final AmazonSQS amazonSQS;
    private final String snsTopicArn;
    private final String sqsQueueUrl;
    
    /**
     * Constructor for NotificationService.
     * 
     * @param amazonSNS SNS client for publishing notifications
     * @param amazonSQS SQS client for queuing messages
     * @param awsConfig Configuration for AWS services
     */
    @Autowired
    public NotificationService(AmazonSNS amazonSNS, AmazonSQS amazonSQS, AwsConfig awsConfig) {
        this.amazonSNS = amazonSNS;
        this.amazonSQS = amazonSQS;
        this.snsTopicArn = awsConfig.getSnsTopicArn();
        this.sqsQueueUrl = awsConfig.getSqsQueueUrl();
    }
    
    /**
     * Initialization method to verify connectivity to AWS services.
     */
    @PostConstruct
    public void initialize() {
        // Verify SNS and SQS connectivity on startup
        try {
            // Simple validation of SNS and SQS clients
            if (amazonSNS != null && amazonSQS != null) {
                System.out.println("NotificationService initialized successfully");
            } else {
                System.err.println("NotificationService initialization failed: AWS clients not available");
            }
        } catch (Exception e) {
            System.err.println("Error during NotificationService initialization: " + e.getMessage());
            // TODO: Implement proper error handling and recovery mechanism
        }
    }

    /**
     * Send notification when a product is updated.
     *
     * @param productId the ID of the product that was updated
     * @param updateType the type of update (e.g., "DETAILS_UPDATED", "IMAGE_UPDATED")
     */
    public void sendProductUpdateNotification(String productId, String updateType) {
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        try {
            // Create message attributes for filtering
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("notificationType", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue("PRODUCT_UPDATE"));
            
            // Create message body
            String message = String.format(
                "Product with ID '%s' has been updated. Update type: %s", 
                productId, updateType);
                
            // Build the publish request
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(snsTopicArn)
                .withMessage(message)
                .withSubject("Product Update Notification")
                .withMessageAttributes(messageAttributes);
                
            // Send the notification
            PublishResult result = amazonSNS.publish(publishRequest);
            
            // Log success
            System.out.println("Product update notification sent successfully. Message ID: " 
                + result.getMessageId());
                
        } catch (Exception e) {
            // Log error and potentially retry
            System.err.println("Failed to send product update notification: " + e.getMessage());
            // TODO: Implement retry logic for transient failures
            
            // Re-throw as runtime exception to be handled by global exception handler
            throw new RuntimeException("Failed to send product update notification", e);
        }
    }

    /**
     * Send alert notification for low inventory.
     *
     * @param productId the ID of the product with low inventory
     * @param currentStock the current stock level
     */
    public void sendInventoryAlert(String productId, int currentStock) {
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        if (currentStock < 0) {
            throw new IllegalArgumentException("Current stock cannot be negative");
        }
        
        try {
            // Determine alert severity based on stock level
            String alertLevel = determineAlertLevel(currentStock);
            
            // Create message attributes for filtering
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("notificationType", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue("INVENTORY_ALERT"));
                    
            messageAttributes.put("alertLevel", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(alertLevel));
            
            // Create message body
            String message = String.format(
                "INVENTORY ALERT: Product '%s' has low stock. Current inventory: %d units", 
                productId, currentStock);
                
            // Build the publish request
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(snsTopicArn)
                .withMessage(message)
                .withSubject("Inventory Alert - " + alertLevel)
                .withMessageAttributes(messageAttributes);
                
            // Send the notification
            PublishResult result = amazonSNS.publish(publishRequest);
            
            // Log success
            System.out.println("Inventory alert sent successfully. Message ID: " 
                + result.getMessageId() + ", Alert Level: " + alertLevel);
                
        } catch (Exception e) {
            // Log error
            System.err.println("Failed to send inventory alert: " + e.getMessage());
            
            // FIXME: Add proper error handling and retry mechanism
            throw new RuntimeException("Failed to send inventory alert notification", e);
        }
    }
    
    /**
     * Helper method to determine alert level based on stock amount.
     */
    private String determineAlertLevel(int stockAmount) {
        if (stockAmount == 0) {
            return "CRITICAL";
        } else if (stockAmount <= 5) {
            return "HIGH";
        } else if (stockAmount <= 10) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Queue message to update user recommendations based on user activity.
     *
     * @param userId the ID of the user
     * @param activityType the type of activity performed (e.g., "VIEW", "PURCHASE")
     */
    public void queueRecommendationUpdate(String userId, String activityType) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        if (activityType == null || activityType.isEmpty()) {
            throw new IllegalArgumentException("Activity type cannot be null or empty");
        }
        
        try {
            // Create a unique message ID
            String messageId = UUID.randomUUID().toString();
            
            // Create message body as JSON
            String messageBody = String.format(
                "{\"userId\":\"%s\",\"activityType\":\"%s\",\"timestamp\":%d,\"messageId\":\"%s\"}", 
                userId, 
                activityType,
                System.currentTimeMillis(),
                messageId);
                
            // Create send message request
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(sqsQueueUrl)
                .withMessageBody(messageBody)
                .withDelaySeconds(0); // No delay
                
            // Add message attributes if needed
            Map<String, String> messageAttributes = new HashMap<>();
            messageAttributes.put("messageType", "RECOMMENDATION_UPDATE");
                
            // Send the message
            SendMessageResult result = amazonSQS.sendMessage(sendMessageRequest);
            
            // Log success
            System.out.println("Recommendation update queued successfully. Message ID: " 
                + result.getMessageId());
                
        } catch (Exception e) {
            // Log error
            System.err.println("Failed to queue recommendation update: " + e.getMessage());
            
            // TODO: Implement retry mechanism with exponential backoff
            throw new RuntimeException("Failed to queue recommendation update", e);
        }
    }

    /**
     * Send notification for product price changes.
     *
     * @param productId the ID of the product with price change
     * @param oldPrice the previous price
     * @param newPrice the new price
     */
    public void sendPriceChangeNotification(String productId, BigDecimal oldPrice, BigDecimal newPrice) {
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        if (oldPrice == null || newPrice == null) {
            throw new IllegalArgumentException("Price values cannot be null");
        }
        
        try {
            // Calculate price difference percentage
            BigDecimal priceDifference = newPrice.subtract(oldPrice);
            BigDecimal percentageChange = priceDifference.multiply(new BigDecimal(100))
                .divide(oldPrice, 2, BigDecimal.ROUND_HALF_UP);
            
            // Determine if it's a price increase or decrease
            String changeType = priceDifference.compareTo(BigDecimal.ZERO) >= 0 ? "increase" : "decrease";
            
            // Create message attributes for filtering
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("notificationType", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue("PRICE_CHANGE"));
                    
            messageAttributes.put("changeType", 
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(changeType));
            
            // Create message body
            String message = String.format(
                "Product '%s' price has changed from %s to %s (%s%.2f%%)", 
                productId,
                oldPrice.toString(),
                newPrice.toString(),
                priceDifference.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "",
                percentageChange);
                
            // Build the publish request
            PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(snsTopicArn)
                .withMessage(message)
                .withSubject("Price Change Notification")
                .withMessageAttributes(messageAttributes);
                
            // Send the notification
            PublishResult result = amazonSNS.publish(publishRequest);
            
            // Log success
            System.out.println("Price change notification sent successfully. Message ID: " 
                + result.getMessageId());
                
        } catch (ArithmeticException ae) {
            // Handle potential division by zero if old price was zero
            System.err.println("Arithmetic error in price calculation: " + ae.getMessage());
            // FIXME: Handle case where old price is zero to prevent division by zero
            throw new RuntimeException("Error in price change calculation", ae);
            
        } catch (Exception e) {
            // Log general errors
            System.err.println("Failed to send price change notification: " + e.getMessage());
            throw new RuntimeException("Failed to send price change notification", e);
        }
    }
}