package com.gradlemedium200.userservice.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SetSMSAttributesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for AWS SNS client setup and topic management.
 * This class initializes the SNS client and manages notification topics.
 */
@Configuration
public class AwsSnsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsSnsConfig.class);
    
    @Value("${aws.credentials.accessKey}")
    private String awsAccessKey;
    
    @Value("${aws.credentials.secretKey}")
    private String awsSecretKey;
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Value("${aws.sns.defaultTopic}")
    private String defaultTopicName;
    
    @Value("${aws.sns.emailTopic}")
    private String emailTopicName;
    
    @Value("${aws.sns.smsTopic}")
    private String smsTopicName;
    
    @Value("${aws.sns.pushNotificationTopic}")
    private String pushNotificationTopicName;
    
    @Value("${aws.sns.smsSenderId:GradleMed}")
    private String smsSenderId;
    
    private String defaultTopicArn;
    private String emailTopicArn;
    private String smsTopicArn;
    private String pushNotificationTopicArn;
    private AmazonSNS snsClient;
    
    /**
     * Initialize the SNS client and create topics if they don't exist.
     */
    @PostConstruct
    public void init() {
        snsClient = getAmazonSNSClient();
        createTopics();
        configureSmsAttributes();
    }
    
    /**
     * Creates the Amazon SNS client using credentials and region.
     * 
     * @return Configured AmazonSNS client
     */
    @Bean
    public AmazonSNS getAmazonSNSClient() {
        if (snsClient == null) {
            AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            snsClient = AmazonSNSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.fromName(awsRegion))
                    .build();
            
            logger.info("AWS SNS client initialized for region: {}", awsRegion);
        }
        return snsClient;
    }
    
    /**
     * Creates SNS topics if they don't exist.
     */
    private void createTopics() {
        try {
            defaultTopicArn = createTopic(defaultTopicName);
            emailTopicArn = createTopic(emailTopicName);
            smsTopicArn = createTopic(smsTopicName);
            pushNotificationTopicArn = createTopic(pushNotificationTopicName);
            
            logger.info("SNS Topics initialized successfully");
        } catch (Exception e) {
            logger.error("Error creating SNS topics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SNS topics", e);
        }
    }
    
    /**
     * Creates an SNS topic with the given name.
     * 
     * @param topicName The name of the topic to create
     * @return The ARN of the created or existing topic
     */
    private String createTopic(String topicName) {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
        CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
        logger.debug("Created or found SNS topic: {}, ARN: {}", topicName, createTopicResult.getTopicArn());
        return createTopicResult.getTopicArn();
    }
    
    /**
     * Configures SMS attributes for the SNS client.
     */
    private void configureSmsAttributes() {
        try {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("DefaultSenderID", smsSenderId);
            attributes.put("DefaultSMSType", "Transactional"); // Use Transactional for higher reliability
            
            SetSMSAttributesRequest setSMSAttributesRequest = new SetSMSAttributesRequest()
                    .withAttributes(attributes);
            
            snsClient.setSMSAttributes(setSMSAttributesRequest);
            logger.info("SNS SMS attributes configured successfully");
        } catch (Exception e) {
            logger.error("Error configuring SNS SMS attributes: {}", e.getMessage(), e);
            // Continue without failing - SMS might still work with default settings
        }
    }
    
    /**
     * Get the ARN of the default topic.
     * 
     * @return The default topic ARN
     */
    public String getDefaultTopicArn() {
        return defaultTopicArn;
    }
    
    /**
     * Get the ARN of the email topic.
     * 
     * @return The email topic ARN
     */
    public String getEmailTopicArn() {
        return emailTopicArn;
    }
    
    /**
     * Get the ARN of the SMS topic.
     * 
     * @return The SMS topic ARN
     */
    public String getSmsTopicArn() {
        return smsTopicArn;
    }
    
    /**
     * Get the ARN of the push notification topic.
     * 
     * @return The push notification topic ARN
     */
    public String getPushNotificationTopicArn() {
        return pushNotificationTopicArn;
    }
    
    /**
     * Get the SMS sender ID.
     * 
     * @return The configured SMS sender ID
     */
    public String getSmsSenderId() {
        return smsSenderId;
    }
}