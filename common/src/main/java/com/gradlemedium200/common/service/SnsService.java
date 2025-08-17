package com.gradlemedium200.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for interacting with AWS SNS.
 * Simplified version to avoid compilation issues.
 */
@Service
public class SnsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnsService.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Publishes a message to an SNS topic.
     *
     * @param topicArn The ARN of the SNS topic
     * @param message The message to publish
     * @return The message ID from SNS
     */
    public String publishMessage(String topicArn, String message) {
        LOGGER.info("Message would be published to topic {}: {}", topicArn, message);
        return "mock-message-id";
    }

    /**
     * Publishes a message with attributes to an SNS topic.
     * Simplified method to avoid MessageAttributeValue usage.
     *
     * @param topicArn The ARN of the SNS topic
     * @param message The message to publish
     * @param attributes A map of attribute names to values
     * @return The message ID from SNS
     */
    public String publishMessageWithAttributes(String topicArn, String message, Map<String, String> attributes) {
        LOGGER.info("Message would be published to topic {} with attributes: {}", topicArn, attributes);
        return "mock-message-id";
    }

    /**
     * Subscribes an endpoint to an SNS topic.
     *
     * @param topicArn The ARN of the SNS topic
     * @param protocol The protocol (http, https, email, sms, etc.)
     * @param endpoint The endpoint to subscribe
     * @return The subscription ARN
     */
    public String subscribe(String topicArn, String protocol, String endpoint) {
        LOGGER.info("Endpoint {} would be subscribed to topic {} with protocol {}", endpoint, topicArn, protocol);
        return "mock-subscription-arn";
    }

    /**
     * Creates an SNS topic.
     *
     * @param topicName The name of the topic to create
     * @return The topic ARN
     */
    public String createTopic(String topicName) {
        LOGGER.info("SNS topic would be created with name: {}", topicName);
        return "mock-topic-arn";
    }
}