package com.gradlemedium200.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AWS SNS publisher service for sending notifications and events.
 * Provides functionality to publish messages to SNS topics with retry capabilities.
 */
@Service
public class SnsPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SnsPublisher.class);
    
    /**
     * Amazon SNS client for interacting with the SNS service
     */
    private final AmazonSNS amazonSNS;
    
    /**
     * Default SNS topic ARN used when no specific topic is provided
     */
    private final String defaultTopicArn;
    
    /**
     * Number of retry attempts for failed publish operations
     */
    private final int retryCount;
    
    /**
     * Constructs an SnsPublisher with the specified parameters.
     *
     * @param amazonSNS the Amazon SNS client
     * @param defaultTopicArn the default topic ARN to use when no specific topic is provided
     * @param retryCount the number of retry attempts for failed publish operations
     */
    @Autowired
    public SnsPublisher(
            AmazonSNS amazonSNS,
            @Value("${aws.sns.default-topic-arn:}") String defaultTopicArn,
            @Value("${aws.sns.retry-count:3}") int retryCount) {
        this.amazonSNS = amazonSNS;
        this.defaultTopicArn = defaultTopicArn;
        this.retryCount = retryCount;
        
        logger.info("SnsPublisher initialized with default topic ARN: {}, retry count: {}", 
                defaultTopicArn != null && !defaultTopicArn.isEmpty() ? defaultTopicArn : "not set", 
                retryCount);
    }
    
    /**
     * Publishes a message to the default SNS topic.
     *
     * @param message the message to be published
     * @return the message ID of the published message
     * @throws IllegalStateException if the default topic ARN is not set
     */
    public String publish(String message) {
        if (defaultTopicArn == null || defaultTopicArn.isEmpty()) {
            logger.error("Default topic ARN is not set");
            throw new IllegalStateException("Default topic ARN is not set. Use publishToTopic() method with a specific topic ARN instead.");
        }
        return publishToTopic(defaultTopicArn, message);
    }
    
    /**
     * Publishes a message to a specific SNS topic.
     *
     * @param topicArn the ARN of the topic to publish to
     * @param message the message to be published
     * @return the message ID of the published message
     */
    public String publish(String topicArn, String message) {
        return publishToTopic(topicArn, message);
    }
    
    /**
     * Publishes a message to a specific SNS topic.
     *
     * @param topicArn the ARN of the topic to publish to
     * @param message the message to be published
     * @return the message ID of the published message
     */
    public String publishToTopic(String topicArn, String message) {
        logger.debug("Publishing message to topic: {}", topicArn);
        
        PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message);
        
        return executeWithRetry(() -> {
            PublishResult result = amazonSNS.publish(publishRequest);
            logger.debug("Message published with ID: {}", result.getMessageId());
            return result.getMessageId();
        });
    }
    
    /**
     * Publishes a message with custom attributes to a specific SNS topic.
     *
     * @param topicArn the ARN of the topic to publish to
     * @param message the message to be published
     * @param attributes the custom message attributes to include
     * @return the message ID of the published message
     */
    public String publishWithAttributes(String topicArn, String message, Map<String, MessageAttributeValue> attributes) {
        logger.debug("Publishing message with attributes to topic: {}", topicArn);
        
        PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message)
                .withMessageAttributes(attributes);
        
        return executeWithRetry(() -> {
            PublishResult result = amazonSNS.publish(publishRequest);
            logger.debug("Message published with ID: {} and {} attributes", 
                    result.getMessageId(), attributes != null ? attributes.size() : 0);
            return result.getMessageId();
        });
    }
    
    /**
     * Creates a new SNS topic with the specified name.
     *
     * @param topicName the name of the topic to create
     * @return the ARN of the created topic
     */
    public String createTopic(String topicName) {
        logger.info("Creating new SNS topic: {}", topicName);
        
        CreateTopicRequest createTopicRequest = new CreateTopicRequest()
                .withName(topicName);
        
        return executeWithRetry(() -> {
            CreateTopicResult result = amazonSNS.createTopic(createTopicRequest);
            logger.info("SNS topic created with ARN: {}", result.getTopicArn());
            return result.getTopicArn();
        });
    }
    
    /**
     * Executes an operation with retry logic.
     *
     * @param operation the operation to execute
     * @return the result of the operation
     * @throws RuntimeException if all retry attempts fail
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        RuntimeException lastException = null;
        
        while (attempts <= retryCount) {
            try {
                return operation.execute();
            } catch (RuntimeException e) {
                lastException = e;
                attempts++;
                
                if (attempts <= retryCount) {
                    int backoffMillis = calculateExponentialBackoff(attempts);
                    logger.warn("Attempt {} failed. Retrying in {} ms. Error: {}", 
                            attempts, backoffMillis, e.getMessage());
                    
                    try {
                        TimeUnit.MILLISECONDS.sleep(backoffMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        logger.error("Failed to execute operation after {} attempts", retryCount + 1);
        throw new RuntimeException("Operation failed after " + (retryCount + 1) + " attempts", lastException);
    }
    
    /**
     * Calculates exponential backoff time based on retry attempt number.
     *
     * @param attempt the current attempt number
     * @return backoff time in milliseconds
     */
    private int calculateExponentialBackoff(int attempt) {
        // Base delay is 100ms, multiplied by 2^attempt with a max of 5 seconds
        // Using min to prevent overflow when calculating 2^attempt
        return (int) Math.min(100 * Math.pow(2, attempt), 5000);
    }
    
    /**
     * Functional interface for operations that can be retried.
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute();
    }
}