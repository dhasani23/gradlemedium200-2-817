package com.gradlemedium200.userservice.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for AWS SQS client setup and queue management.
 * This class initializes the SQS client and manages notification queues.
 */
@Configuration
public class AwsSqsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsSqsConfig.class);
    
    @Value("${aws.credentials.accessKey}")
    private String awsAccessKey;
    
    @Value("${aws.credentials.secretKey}")
    private String awsSecretKey;
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Value("${aws.sqs.notificationQueue}")
    private String notificationQueueName;
    
    @Value("${aws.sqs.deadLetterQueue}")
    private String deadLetterQueueName;
    
    @Value("${aws.sqs.messageRetentionPeriod:1209600}") // Default 14 days
    private Integer messageRetentionPeriod;
    
    @Value("${aws.sqs.visibilityTimeout:30}") // Default 30 seconds
    private Integer visibilityTimeout;
    
    private String notificationQueueUrl;
    private String deadLetterQueueUrl;
    private AmazonSQS sqsClient;
    
    /**
     * Initialize the SQS client and create queues if they don't exist.
     */
    @PostConstruct
    public void init() {
        sqsClient = getAmazonSQSClient();
        createQueues();
    }
    
    /**
     * Creates the Amazon SQS client using credentials and region.
     * 
     * @return Configured AmazonSQS client
     */
    @Bean
    public AmazonSQS getAmazonSQSClient() {
        if (sqsClient == null) {
            AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            sqsClient = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.fromName(awsRegion))
                    .build();
            
            logger.info("AWS SQS client initialized for region: {}", awsRegion);
        }
        return sqsClient;
    }
    
    /**
     * Creates required SQS queues if they don't exist.
     */
    private void createQueues() {
        try {
            // Create dead letter queue first
            deadLetterQueueUrl = findOrCreateQueue(deadLetterQueueName, null);
            
            // Get the ARN of the DLQ for reference in main queue
            String deadLetterQueueArn = sqsClient.getQueueAttributes(deadLetterQueueUrl, 
                    java.util.Collections.singletonList("QueueArn"))
                    .getAttributes().get("QueueArn");
            
            // Configure main queue with dead letter queue redirection
            Map<String, String> attributes = new HashMap<>();
            
            // Create redirection policy to dead letter queue
            Map<String, String> redrivePolicy = new HashMap<>();
            redrivePolicy.put("maxReceiveCount", "5");
            redrivePolicy.put("deadLetterTargetArn", deadLetterQueueArn);
            
            // Add queue attributes
            attributes.put("MessageRetentionPeriod", messageRetentionPeriod.toString());
            attributes.put("VisibilityTimeout", visibilityTimeout.toString());
            attributes.put("RedrivePolicy", redrivePolicy.toString());
            
            // Create main queue
            notificationQueueUrl = findOrCreateQueue(notificationQueueName, attributes);
            
            logger.info("SQS Queues initialized successfully. Main queue: {}, DLQ: {}", 
                    notificationQueueName, deadLetterQueueName);
        } catch (Exception e) {
            logger.error("Error creating SQS queues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SQS queues", e);
        }
    }
    
    /**
     * Finds an existing queue or creates it if it doesn't exist.
     * 
     * @param queueName The name of the queue
     * @param attributes Queue attributes for creation
     * @return The URL of the queue
     */
    private String findOrCreateQueue(String queueName, Map<String, String> attributes) {
        try {
            // Try to get the queue URL
            return sqsClient.getQueueUrl(queueName).getQueueUrl();
        } catch (QueueDoesNotExistException e) {
            // Queue doesn't exist, create it
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
            if (attributes != null) {
                createQueueRequest.setAttributes(attributes);
            }
            
            return sqsClient.createQueue(createQueueRequest).getQueueUrl();
        }
    }
    
    /**
     * Get the URL of the notification queue.
     * 
     * @return The notification queue URL
     */
    public String getNotificationQueueUrl() {
        return notificationQueueUrl;
    }
    
    /**
     * Get the URL of the dead letter queue.
     * 
     * @return The dead letter queue URL
     */
    public String getDeadLetterQueueUrl() {
        return deadLetterQueueUrl;
    }
}