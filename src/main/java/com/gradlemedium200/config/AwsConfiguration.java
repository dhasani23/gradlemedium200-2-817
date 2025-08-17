package com.gradlemedium200.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS services configuration class for the root module.
 * Provides client beans for AWS SNS, SQS, and DynamoDB services.
 * 
 * This configuration sets up the AWS clients with proper region and credentials
 * to be used across the application for various AWS service interactions.
 */
@Configuration
public class AwsConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsConfiguration.class);
    
    /**
     * AWS region for service connections
     */
    @Value("${aws.region:us-east-1}")
    private String region;
    
    /**
     * AWS access key ID
     */
    @Value("${aws.credentials.access-key}")
    private String accessKey;
    
    /**
     * AWS secret access key
     */
    @Value("${aws.credentials.secret-key}")
    private String secretKey;
    
    /**
     * Creates Amazon SNS client bean for notification services
     * 
     * @return Configured Amazon SNS client
     */
    @Bean
    @Primary
    public AmazonSNS amazonSNS() {
        logger.info("Initializing Amazon SNS client for region: {}", region);
        try {
            return AmazonSNSClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(awsCredentialsProvider())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize Amazon SNS client", e);
            throw new RuntimeException("Could not create Amazon SNS client", e);
        }
    }
    
    /**
     * Creates Amazon SQS client bean for message queueing
     * 
     * @return Configured Amazon SQS client
     */
    @Bean
    @Primary
    public AmazonSQS amazonSQS() {
        logger.info("Initializing Amazon SQS client for region: {}", region);
        try {
            return AmazonSQSClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(awsCredentialsProvider())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize Amazon SQS client", e);
            throw new RuntimeException("Could not create Amazon SQS client", e);
        }
    }
    
    /**
     * Creates Amazon DynamoDB client bean for NoSQL database operations
     * 
     * @return Configured Amazon DynamoDB client
     */
    @Bean
    @Primary
    public AmazonDynamoDB amazonDynamoDB() {
        logger.info("Initializing Amazon DynamoDB client for region: {}", region);
        try {
            return AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(awsCredentialsProvider())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize Amazon DynamoDB client", e);
            throw new RuntimeException("Could not create Amazon DynamoDB client", e);
        }
    }
    
    /**
     * Creates AWS credentials provider using the configured access and secret keys
     * 
     * @return AWS credentials provider for client authentication
     */
    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        // TODO: Consider using environment-specific credential providers for production
        // e.g., instance profiles for EC2, container roles for ECS/EKS
        
        // FIXME: Secure the handling of credentials, avoid hardcoding in properties
        if (accessKey == null || secretKey == null) {
            logger.warn("AWS credentials not fully configured, this may cause issues with AWS service connections");
        }
        
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AWSStaticCredentialsProvider(credentials);
    }
}