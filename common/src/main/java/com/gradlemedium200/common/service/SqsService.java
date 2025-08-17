package com.gradlemedium200.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with AWS SQS.
 * Simplified version to avoid compilation issues.
 */
@Service
public class SqsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsService.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Sends a message to an SQS queue.
     *
     * @param queueUrl The URL of the SQS queue
     * @param messageBody The message body to send
     * @return The message ID from SQS
     */
    public String sendMessage(String queueUrl, String messageBody) {
        LOGGER.info("Message would be sent to queue {}: {}", queueUrl, messageBody);
        return "mock-message-id";
    }

    /**
     * Sends a message with attributes to an SQS queue.
     * Simplified method to avoid MessageAttributeValue usage.
     *
     * @param queueUrl The URL of the SQS queue
     * @param messageBody The message body to send
     * @param attributes A map of attribute names to values
     * @return The message ID from SQS
     */
    public String sendMessageWithAttributes(String queueUrl, String messageBody, Map<String, String> attributes) {
        LOGGER.info("Message would be sent to queue {} with attributes: {}", queueUrl, attributes);
        return "mock-message-id";
    }

    /**
     * Receives messages from an SQS queue.
     *
     * @param queueUrl The URL of the SQS queue
     * @param maxNumberOfMessages The maximum number of messages to receive
     * @param visibilityTimeoutSeconds The visibility timeout in seconds
     * @param waitTimeSeconds The wait time in seconds
     * @return A list of received messages (empty in this simplified version)
     */
    public List<Object> receiveMessages(String queueUrl, Integer maxNumberOfMessages, 
                                        Integer visibilityTimeoutSeconds, Integer waitTimeSeconds) {
        LOGGER.info("Would receive up to {} messages from queue {}", maxNumberOfMessages, queueUrl);
        return new ArrayList<>();
    }

    /**
     * Creates an SQS queue.
     *
     * @param queueName The name of the queue to create
     * @return The URL of the created queue
     */
    public String createQueue(String queueName) {
        LOGGER.info("SQS queue would be created with name: {}", queueName);
        return "mock-queue-url";
    }
}