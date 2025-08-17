package com.gradlemedium200.notification.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS SNS integration service for publishing messages to SNS topics 
 * with support for different message formats and delivery confirmation.
 * 
 * This service handles email notifications, SMS notifications, and mobile 
 * push notifications through the AWS SNS platform.
 */
@Service
public class SnsPublisher {
    private static final Logger logger = LoggerFactory.getLogger(SnsPublisher.class);

    private final AmazonSNS amazonSNS;
    
    @Value("${aws.sns.email.topic.arn}")
    private String emailTopicArn;
    
    @Value("${aws.sns.sms.topic.arn}")
    private String smsTopicArn;
    
    @Value("${aws.sns.push.topic.arn}")
    private String pushTopicArn;

    @Autowired
    public SnsPublisher(AmazonSNS amazonSNS) {
        this.amazonSNS = amazonSNS;
    }

    /**
     * Publishes an email message to SNS topic.
     * 
     * @param subject Email subject
     * @param message Email body
     * @param targetArn Optional target ARN, if not provided uses default email topic
     * @return Message ID of the published message
     */
    public String publishEmailMessage(String subject, String message, String targetArn) {
        logger.info("Publishing email message with subject: {}", subject);
        
        String topicArn = targetArn != null ? targetArn : emailTopicArn;
        
        try {
            PublishResult result = publishToTopic(topicArn, message, subject);
            logger.debug("Email message published successfully. Message ID: {}", result.getMessageId());
            return result.getMessageId();
        } catch (Exception e) {
            logger.error("Failed to publish email message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish email message", e);
        }
    }
    
    /**
     * Publishes an SMS message to SNS.
     * 
     * @param message SMS content
     * @param phoneNumber Recipient's phone number in E.164 format
     * @return Message ID of the published message
     */
    public String publishSmsMessage(String message, String phoneNumber) {
        logger.info("Publishing SMS message to phone number: {}", phoneNumber);
        
        try {
            // Direct publish to phone number
            PublishRequest request = new PublishRequest()
                    .withMessage(message)
                    .withPhoneNumber(phoneNumber);
            
            PublishResult result = amazonSNS.publish(request);
            logger.debug("SMS message published successfully. Message ID: {}", result.getMessageId());
            return result.getMessageId();
        } catch (Exception e) {
            logger.error("Failed to publish SMS message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish SMS message", e);
        }
    }
    
    /**
     * Publishes a push notification message to SNS.
     * 
     * @param message Push notification content
     * @param platformApplicationArn The platform application ARN
     * @param deviceToken Target device token
     * @return Message ID of the published message
     */
    public String publishPushMessage(String message, String platformApplicationArn, String deviceToken) {
        logger.info("Publishing push notification to platform: {}", platformApplicationArn);
        
        try {
            // Create platform endpoint if not already created
            // TODO: Implement endpoint caching mechanism for better performance
            
            // For direct delivery to a device endpoint
            PublishRequest request = new PublishRequest()
                    .withMessage(message)
                    .withTargetArn(deviceToken);
            
            PublishResult result = amazonSNS.publish(request);
            logger.debug("Push notification published successfully. Message ID: {}", result.getMessageId());
            return result.getMessageId();
        } catch (Exception e) {
            logger.error("Failed to publish push notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish push notification", e);
        }
    }
    
    /**
     * Generic method to publish message to any SNS topic.
     * 
     * @param topicArn The ARN of the topic to publish to
     * @param message The message content
     * @param subject Optional subject for the message (used in email delivery)
     * @return PublishResult containing the message ID and details
     */
    public PublishResult publishToTopic(String topicArn, String message, String subject) {
        logger.debug("Publishing message to topic: {}", topicArn);
        
        PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message);
        
        if (subject != null && !subject.isEmpty()) {
            publishRequest.setSubject(subject);
        }
        
        return amazonSNS.publish(publishRequest);
    }
    
    /**
     * Publishes a message with custom attributes to SNS topic.
     * 
     * @param message The message content
     * @param topicArn The ARN of the topic to publish to
     * @param attributes Custom attributes to include with the message
     * @return Message ID of the published message
     */
    public String publishWithAttributes(String message, String topicArn, Map<String, String> attributes) {
        logger.info("Publishing message with {} attributes to topic: {}", 
                attributes != null ? attributes.size() : 0, topicArn);
        
        try {
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(message);
            
            if (attributes != null && !attributes.isEmpty()) {
                Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                
                attributes.forEach((key, value) -> {
                    messageAttributes.put(key, new MessageAttributeValue()
                            .withDataType("String")
                            .withStringValue(value));
                });
                
                publishRequest.setMessageAttributes(messageAttributes);
            }
            
            PublishResult result = amazonSNS.publish(publishRequest);
            logger.debug("Message with attributes published successfully. Message ID: {}", result.getMessageId());
            return result.getMessageId();
        } catch (Exception e) {
            logger.error("Failed to publish message with attributes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish message with attributes", e);
        }
    }
    
    /**
     * Creates a new SNS topic.
     * 
     * @param topicName Name for the new topic
     * @return The ARN of the created topic
     */
    public String createTopic(String topicName) {
        logger.info("Creating new SNS topic: {}", topicName);
        
        try {
            CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
            CreateTopicResult createTopicResult = amazonSNS.createTopic(createTopicRequest);
            
            String topicArn = createTopicResult.getTopicArn();
            logger.info("Successfully created topic. ARN: {}", topicArn);
            
            return topicArn;
        } catch (Exception e) {
            logger.error("Failed to create topic {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to create SNS topic", e);
        }
    }
    
    /**
     * Subscribes an endpoint to an SNS topic.
     * 
     * @param topicArn The ARN of the topic to subscribe to
     * @param protocol The protocol to use (e.g., "email", "sms", "application", "lambda")
     * @param endpoint The endpoint to receive notifications
     * @return The subscription ARN
     */
    public String subscribe(String topicArn, String protocol, String endpoint) {
        logger.info("Subscribing {} endpoint {} to topic: {}", protocol, endpoint, topicArn);
        
        try {
            SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, protocol, endpoint);
            SubscribeResult subscribeResult = amazonSNS.subscribe(subscribeRequest);
            
            String subscriptionArn = subscribeResult.getSubscriptionArn();
            logger.info("Successfully created subscription. ARN: {}", subscriptionArn);
            
            // FIXME: For email protocol, the subscription is pending confirmation and returns "pending confirmation"
            // We should provide a way to check the confirmation status later
            
            return subscriptionArn;
        } catch (Exception e) {
            logger.error("Failed to subscribe endpoint to topic: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create subscription", e);
        }
    }
}