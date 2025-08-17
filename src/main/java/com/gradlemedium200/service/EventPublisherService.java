package com.gradlemedium200.service;

import com.gradlemedium200.aws.SnsPublisher;
import com.gradlemedium200.aws.SqsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for publishing events to AWS SNS and SQS for inter-service communication.
 * This service handles routing of different types of events to appropriate AWS channels.
 */
@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    private final SnsPublisher snsPublisher;
    private final SqsPublisher sqsPublisher;
    private Map<String, String> eventMapping;

    @Autowired
    public EventPublisherService(SnsPublisher snsPublisher, SqsPublisher sqsPublisher) {
        this.snsPublisher = snsPublisher;
        this.sqsPublisher = sqsPublisher;
        this.eventMapping = new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        // Initialize event type to destination mappings
        // User events
        eventMapping.put("USER_CREATED", "USER_EVENTS_TOPIC");
        eventMapping.put("USER_UPDATED", "USER_EVENTS_TOPIC");
        eventMapping.put("USER_DELETED", "USER_EVENTS_TOPIC");
        eventMapping.put("USER_LOGIN", "USER_ACTIVITY_QUEUE");
        eventMapping.put("USER_LOGOUT", "USER_ACTIVITY_QUEUE");

        // Order events
        eventMapping.put("ORDER_CREATED", "ORDER_EVENTS_TOPIC");
        eventMapping.put("ORDER_UPDATED", "ORDER_EVENTS_TOPIC");
        eventMapping.put("ORDER_CANCELLED", "ORDER_EVENTS_TOPIC");
        eventMapping.put("ORDER_SHIPPED", "ORDER_NOTIFICATIONS_TOPIC");
        eventMapping.put("ORDER_DELIVERED", "ORDER_NOTIFICATIONS_TOPIC");
        
        // System events
        eventMapping.put("SYSTEM_STARTUP", "SYSTEM_EVENTS_TOPIC");
        eventMapping.put("SYSTEM_SHUTDOWN", "SYSTEM_EVENTS_TOPIC");
        eventMapping.put("SYSTEM_ERROR", "SYSTEM_ALERTS_TOPIC");
        eventMapping.put("SYSTEM_WARNING", "SYSTEM_ALERTS_TOPIC");

        logger.info("EventPublisherService initialized with {} event mappings", eventMapping.size());
    }
    
    /**
     * General method to publish events with string type and map data.
     * Routes the event to the appropriate channel based on the event type.
     * 
     * @param eventType The type of event
     * @param eventData The event data as a Map
     */
    public void publishEvent(String eventType, Map<String, Object> eventData) {
        logger.info("Publishing event: {}", eventType);
        
        if (eventType == null || eventType.trim().isEmpty()) {
            logger.error("Cannot publish event with null or empty event type");
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        
        try {
            // Determine event category
            if (eventType.startsWith("user-") || eventType.contains("USER_")) {
                publishUserEvent(eventType.toUpperCase().replace('-', '_'), eventData);
            } else if (eventType.startsWith("order-") || eventType.contains("ORDER_")) {
                publishOrderEvent(eventType.toUpperCase().replace('-', '_'), eventData);
            } else if (eventType.startsWith("system-") || eventType.contains("SYSTEM_")) {
                publishSystemEvent(eventType.toUpperCase().replace('-', '_'), eventData);
            } else {
                // Generic event publishing for other types
                String destination = resolveDestinationByEventType(eventType);
                
                if (destination.endsWith("_TOPIC")) {
                    String topicArn = resolveTopicArn(destination);
                    String message = formatGenericMessage(eventType, eventData);
                    snsPublisher.publish(topicArn, message);
                } else if (destination.endsWith("_QUEUE")) {
                    String queueUrl = resolveQueueUrl(destination);
                    String message = formatGenericMessage(eventType, eventData);
                    sqsPublisher.sendMessage(queueUrl, message);
                } else {
                    logger.warn("Unknown destination format for event type: {}", eventType);
                }
            }
            
            logger.debug("Successfully published event: {}", eventType);
        } catch (Exception e) {
            logger.error("Failed to publish event: {}", eventType, e);
            // TODO: Implement retry mechanism for failed event publishing
        }
    }
    
    /**
     * Publishes an event with string type and object data.
     * 
     * @param eventType The type of event
     * @param eventData The event data as an Object
     */
    public void publishEvent(String eventType, Object eventData) {
        logger.info("Publishing object event: {}", eventType);
        
        if (eventType == null || eventType.trim().isEmpty()) {
            logger.error("Cannot publish event with null or empty event type");
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        
        try {
            // Convert to a standard format
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("data", eventData);
            dataMap.put("timestamp", java.time.Instant.now().toString());
            dataMap.put("type", eventType);
            
            // Use the map-based method
            publishEvent(eventType, dataMap);
        } catch (Exception e) {
            logger.error("Failed to publish object event: {}", eventType, e);
            // TODO: Implement retry mechanism for failed event publishing
        }
    }

    /**
     * Publishes user-related events to appropriate channels based on event type.
     * 
     * @param eventType The type of user event
     * @param userData The user data associated with the event
     */
    public void publishUserEvent(String eventType, Object userData) {
        logger.info("Publishing user event: {}", eventType);
        
        if (userData == null) {
            logger.warn("Cannot publish user event with null data");
            return;
        }

        try {
            String destination = eventMapping.getOrDefault(eventType, "USER_EVENTS_TOPIC");
            
            // Determine if this event should go to SNS or SQS based on destination naming convention
            if (destination.endsWith("_TOPIC")) {
                String topicArn = resolveTopicArn(destination);
                String message = formatUserMessage(eventType, userData);
                snsPublisher.publish(topicArn, message);
                logger.debug("User event {} published to SNS topic {}", eventType, destination);
            } else if (destination.endsWith("_QUEUE")) {
                String queueUrl = resolveQueueUrl(destination);
                String message = formatUserMessage(eventType, userData);
                sqsPublisher.sendMessage(queueUrl, message);
                logger.debug("User event {} published to SQS queue {}", eventType, destination);
            } else {
                logger.warn("Unknown destination format for event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Failed to publish user event: {}", eventType, e);
            // TODO: Implement retry mechanism for failed event publishing
        }
    }

    /**
     * Publishes order-related events to appropriate channels based on event type.
     * 
     * @param eventType The type of order event
     * @param orderData The order data associated with the event
     */
    public void publishOrderEvent(String eventType, Object orderData) {
        logger.info("Publishing order event: {}", eventType);
        
        if (orderData == null) {
            logger.warn("Cannot publish order event with null data");
            return;
        }

        try {
            String destination = eventMapping.getOrDefault(eventType, "ORDER_EVENTS_TOPIC");
            
            // Route high priority order events to SNS for immediate fanout
            if (isHighPriorityOrderEvent(eventType)) {
                String topicArn = resolveTopicArn(destination);
                String message = formatOrderMessage(eventType, orderData);
                snsPublisher.publish(topicArn, message);
                logger.debug("High priority order event {} published to SNS topic {}", eventType, destination);
            } else {
                // Regular order events go to SQS for async processing
                String queueUrl = resolveQueueUrl("ORDER_PROCESSING_QUEUE");
                String message = formatOrderMessage(eventType, orderData);
                sqsPublisher.sendMessage(queueUrl, message);
                logger.debug("Order event {} published to SQS queue", eventType);
            }
        } catch (Exception e) {
            logger.error("Failed to publish order event: {}", eventType, e);
            // FIXME: Error handling is incomplete - implement dead-letter queue
        }
    }

    /**
     * Publishes system-level events to appropriate channels based on event type.
     * 
     * @param eventType The type of system event
     * @param systemData The system data associated with the event
     */
    public void publishSystemEvent(String eventType, Object systemData) {
        logger.info("Publishing system event: {}", eventType);
        
        Objects.requireNonNull(eventType, "Event type cannot be null");
        
        try {
            String destination = eventMapping.getOrDefault(eventType, "SYSTEM_EVENTS_TOPIC");
            String topicArn = resolveTopicArn(destination);
            
            // Format system message based on severity
            String message;
            if (eventType.contains("ERROR") || eventType.contains("ALERT")) {
                message = formatHighPrioritySystemMessage(eventType, systemData);
            } else {
                message = formatSystemMessage(eventType, systemData);
            }
            
            // System events always go through SNS for immediate notification
            publishToTopic(topicArn, message);
            
            // For critical system events, also log to CloudWatch through a specialized queue
            if (eventType.contains("ERROR") || eventType.contains("CRITICAL")) {
                String cloudWatchQueue = resolveQueueUrl("SYSTEM_LOGGING_QUEUE");
                sqsPublisher.sendMessage(cloudWatchQueue, message);
                logger.debug("Critical system event {} also sent to CloudWatch logging queue", eventType);
            }
        } catch (Exception e) {
            logger.error("Failed to publish system event: {}", eventType, e);
            // FIXME: System event failures should trigger alerts - implement alerting mechanism
        }
    }

    /**
     * Publishes message to specific SNS topic.
     * 
     * @param topicArn The ARN of the SNS topic
     * @param message The message to publish
     */
    public void publishToTopic(String topicArn, String message) {
        logger.debug("Publishing message to topic: {}", topicArn);
        
        if (topicArn == null || topicArn.trim().isEmpty()) {
            logger.error("Cannot publish to null or empty topic ARN");
            throw new IllegalArgumentException("Topic ARN cannot be null or empty");
        }
        
        if (message == null) {
            logger.warn("Cannot publish null message to SNS");
            return;
        }
        
        try {
            snsPublisher.publish(topicArn, message);
            logger.debug("Message successfully published to topic: {}", topicArn);
        } catch (Exception e) {
            logger.error("Failed to publish message to topic: {}", topicArn, e);
            throw new RuntimeException("Failed to publish message to SNS topic", e);
        }
    }
    
    // Helper methods
    
    /**
     * Determines the appropriate destination for an event type.
     * 
     * @param eventType The event type
     * @return The resolved destination
     */
    private String resolveDestinationByEventType(String eventType) {
        // First check if we have an explicit mapping
        if (eventMapping.containsKey(eventType)) {
            return eventMapping.get(eventType);
        }
        
        // Otherwise infer based on the event type
        if (eventType.contains("ERROR") || eventType.contains("ALERT")) {
            return "SYSTEM_ALERTS_TOPIC";
        } else if (eventType.contains("LOG") || eventType.contains("INFO")) {
            return "SYSTEM_LOGGING_QUEUE";
        } else {
            return "GENERAL_EVENTS_TOPIC";
        }
    }
    
    /**
     * Format a generic event message.
     * 
     * @param eventType The event type
     * @param eventData The event data
     * @return Formatted message string
     */
    private String formatGenericMessage(String eventType, Object eventData) {
        // Simple formatting for generic messages
        return String.format("{\n  \"eventType\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"data\": %s\n}", 
                eventType, 
                java.time.Instant.now(),
                eventData != null ? eventData.toString() : "null");
    }
    
    private String resolveTopicArn(String topicName) {
        // TODO: Implement dynamic ARN resolution based on environment
        return "arn:aws:sns:us-east-1:123456789012:" + topicName.toLowerCase();
    }
    
    private String resolveQueueUrl(String queueName) {
        // TODO: Implement dynamic queue URL resolution based on environment
        return "https://sqs.us-east-1.amazonaws.com/123456789012/" + queueName.toLowerCase();
    }
    
    private String formatUserMessage(String eventType, Object userData) {
        // TODO: Implement proper message formatting with JSON serialization
        return String.format("{\n  \"eventType\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"data\": %s\n}", 
                eventType, 
                java.time.Instant.now(),
                userData.toString());
    }
    
    private String formatOrderMessage(String eventType, Object orderData) {
        // TODO: Implement proper message formatting with JSON serialization
        return String.format("{\n  \"eventType\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"data\": %s\n}", 
                eventType, 
                java.time.Instant.now(),
                orderData.toString());
    }
    
    private String formatSystemMessage(String eventType, Object systemData) {
        // TODO: Implement proper message formatting with JSON serialization
        return String.format("{\n  \"eventType\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"data\": %s\n}", 
                eventType, 
                java.time.Instant.now(),
                systemData != null ? systemData.toString() : "null");
    }
    
    private String formatHighPrioritySystemMessage(String eventType, Object systemData) {
        // Adding priority field for high priority messages
        return String.format("{\n  \"eventType\": \"%s\",\n  \"priority\": \"HIGH\",\n  \"timestamp\": \"%s\",\n  \"data\": %s\n}", 
                eventType, 
                java.time.Instant.now(),
                systemData != null ? systemData.toString() : "null");
    }
    
    private boolean isHighPriorityOrderEvent(String eventType) {
        return eventType.equals("ORDER_CANCELLED") || 
               eventType.equals("ORDER_SHIPPED") || 
               eventType.equals("ORDER_DELIVERED");
    }
}