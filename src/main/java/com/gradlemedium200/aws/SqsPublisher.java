package com.gradlemedium200.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS SQS publisher for sending messages to queues.
 * 
 * This service provides functionality to send messages to Amazon SQS queues,
 * create new queues, and handle message delivery with configurable options.
 */
@Service
public class SqsPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SqsPublisher.class);
    
    /**
     * Amazon SQS client
     */
    private final AmazonSQS amazonSQS;
    
    /**
     * Default SQS queue URL
     */
    private String defaultQueueUrl;
    
    /**
     * Message retention period in seconds
     */
    private int messageRetention;
    
    /**
     * Constructs a new SQS publisher with default retention period of 7 days (604800 seconds).
     * 
     * @param amazonSQS the Amazon SQS client
     * @param defaultQueueUrl the default queue URL to publish messages to
     */
    @Autowired
    public SqsPublisher(AmazonSQS amazonSQS, 
                       @Value("${aws.sqs.default-queue-url:}") String defaultQueueUrl) {
        this.amazonSQS = amazonSQS;
        this.defaultQueueUrl = defaultQueueUrl;
        this.messageRetention = 604800; // Default to 7 days
        logger.info("SqsPublisher initialized with default queue URL: {}", defaultQueueUrl);
    }
    
    /**
     * Initialize the publisher, validating the default queue URL if provided.
     */
    @PostConstruct
    public void init() {
        if (defaultQueueUrl == null || defaultQueueUrl.isEmpty()) {
            logger.warn("No default queue URL configured. Default queue operations will not work until a queue URL is set.");
        } else {
            try {
                // Validate that the queue exists by getting its attributes
                amazonSQS.getQueueAttributes(defaultQueueUrl, null);
                logger.info("Successfully verified default SQS queue: {}", defaultQueueUrl);
            } catch (Exception e) {
                logger.error("Failed to validate default queue URL: {}. Error: {}", defaultQueueUrl, e.getMessage());
                // Don't throw exception, just warn - the queue might be created later
            }
        }
    }
    
    /**
     * Sends a message to the default queue.
     *
     * @param message the message content to send
     * @return the message ID from SQS if successful
     * @throws IllegalStateException if no default queue is configured
     */
    public String sendMessage(String message) {
        if (defaultQueueUrl == null || defaultQueueUrl.isEmpty()) {
            logger.error("Cannot send message to default queue - no default queue URL configured");
            throw new IllegalStateException("No default queue URL configured");
        }
        
        return sendMessageToQueue(defaultQueueUrl, message);
    }
    
    /**
     * Sends a message to a specific queue.
     *
     * @param queueUrl the URL of the queue to send the message to
     * @param message the message content to send
     * @return the message ID from SQS if successful
     */
    public String sendMessage(String queueUrl, String message) {
        return sendMessageToQueue(queueUrl, message);
    }
    
    /**
     * Sends a message to a specific queue.
     *
     * @param queueUrl the URL of the queue to send the message to
     * @param message the message content to send
     * @return the message ID from SQS if successful
     */
    public String sendMessageToQueue(String queueUrl, String message) {
        logger.debug("Sending message to queue {}: {}", queueUrl, message);
        try {
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(message);
            
            SendMessageResult result = amazonSQS.sendMessage(sendMessageRequest);
            String messageId = result.getMessageId();
            logger.info("Successfully sent message to queue {}, message ID: {}", queueUrl, messageId);
            return messageId;
        } catch (Exception e) {
            logger.error("Failed to send message to queue {}: {}", queueUrl, e.getMessage());
            throw new RuntimeException("Failed to send SQS message", e);
        }
    }
    
    /**
     * Sends a message with a delay to a specific queue.
     *
     * @param queueUrl the URL of the queue to send the message to
     * @param message the message content to send
     * @param delaySeconds the delay in seconds before the message becomes available for processing
     * @return the message ID from SQS if successful
     */
    public String sendDelayedMessage(String queueUrl, String message, int delaySeconds) {
        logger.debug("Sending delayed message to queue {} with delay of {} seconds: {}", 
                     queueUrl, delaySeconds, message);
                     
        if (delaySeconds < 0 || delaySeconds > 900) { // AWS SQS allows 0-900 seconds delay
            throw new IllegalArgumentException("Delay seconds must be between 0 and 900");
        }
        
        try {
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(message)
                    .withDelaySeconds(delaySeconds);
            
            SendMessageResult result = amazonSQS.sendMessage(sendMessageRequest);
            String messageId = result.getMessageId();
            logger.info("Successfully sent delayed message to queue {}, message ID: {}, delay: {} seconds", 
                       queueUrl, messageId, delaySeconds);
            return messageId;
        } catch (Exception e) {
            logger.error("Failed to send delayed message to queue {}: {}", queueUrl, e.getMessage());
            throw new RuntimeException("Failed to send delayed SQS message", e);
        }
    }
    
    /**
     * Creates a new SQS queue and returns its URL.
     *
     * @param queueName the name of the queue to create
     * @return the URL of the created queue
     */
    public String createQueue(String queueName) {
        logger.info("Creating SQS queue with name: {}", queueName);
        try {
            // Set queue attributes
            Map<String, String> attributes = new HashMap<>();
            attributes.put("MessageRetentionPeriod", String.valueOf(messageRetention));
            
            CreateQueueRequest createQueueRequest = new CreateQueueRequest()
                    .withQueueName(queueName)
                    .withAttributes(attributes);
            
            CreateQueueResult result = amazonSQS.createQueue(createQueueRequest);
            String queueUrl = result.getQueueUrl();
            
            logger.info("Successfully created SQS queue: {}, URL: {}", queueName, queueUrl);
            
            // If no default queue is set, use this one as default
            if (defaultQueueUrl == null || defaultQueueUrl.isEmpty()) {
                logger.info("Setting newly created queue as default queue: {}", queueUrl);
                defaultQueueUrl = queueUrl;
            }
            
            return queueUrl;
        } catch (Exception e) {
            logger.error("Failed to create SQS queue {}: {}", queueName, e.getMessage());
            throw new RuntimeException("Failed to create SQS queue", e);
        }
    }
    
    /**
     * Sets the message retention period for newly created queues.
     *
     * @param seconds retention period in seconds
     */
    public void setMessageRetention(int seconds) {
        if (seconds < 60 || seconds > 1209600) { // AWS SQS allows 60 seconds to 14 days
            throw new IllegalArgumentException("Message retention must be between 60 and 1209600 seconds (14 days)");
        }
        this.messageRetention = seconds;
        logger.info("Message retention period set to {} seconds", seconds);
    }
    
    /**
     * Gets the current message retention period setting.
     *
     * @return message retention period in seconds
     */
    public int getMessageRetention() {
        return messageRetention;
    }
    
    /**
     * Sets the default queue URL.
     *
     * @param queueUrl the queue URL to set as default
     */
    public void setDefaultQueueUrl(String queueUrl) {
        this.defaultQueueUrl = queueUrl;
        logger.info("Default queue URL set to: {}", queueUrl);
    }
    
    /**
     * Gets the default queue URL.
     *
     * @return the default queue URL
     */
    public String getDefaultQueueUrl() {
        return defaultQueueUrl;
    }
}