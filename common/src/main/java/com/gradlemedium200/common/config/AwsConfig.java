package com.gradlemedium200.common.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
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

/**
 * AWS Configuration class that provides client beans for AWS services (SNS, SQS, DynamoDB).
 * Configures AWS clients with region, credentials, timeouts, and retry settings.
 */
@Configuration
public class AwsConfig {

    /**
     * AWS region for service clients
     */
    @Value("${aws.region:us-east-1}")
    private String region;
    
    /**
     * AWS access key for authentication
     */
    @Value("${aws.credentials.accessKey}")
    private String accessKey;
    
    /**
     * AWS secret key for authentication
     */
    @Value("${aws.credentials.secretKey}")
    private String secretKey;
    
    /**
     * Socket timeout in milliseconds
     */
    @Value("${aws.client.socketTimeout:50000}")
    private int socketTimeout;
    
    /**
     * Connection timeout in milliseconds
     */
    @Value("${aws.client.connectionTimeout:10000}")
    private int connectionTimeout;
    
    /**
     * Maximum number of retry attempts
     */
    @Value("${aws.client.maxRetries:3}")
    private int maxRetries;
    
    /**
     * Creates and configures an Amazon SNS client
     *
     * @return Configured SNS client
     */
    @Bean
    public AmazonSNS snsClient() {
        try {
            return AmazonSNSClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                    .withClientConfiguration(clientConfiguration())
                    .build();
        } catch (AmazonServiceException e) {
            // FIXME: Replace with proper logging
            System.err.println("Error creating SNS client: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates and configures an Amazon SQS client
     *
     * @return Configured SQS client
     */
    @Bean
    public AmazonSQS sqsClient() {
        try {
            return AmazonSQSClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                    .withClientConfiguration(clientConfiguration())
                    .build();
        } catch (AmazonServiceException e) {
            // FIXME: Replace with proper logging
            System.err.println("Error creating SQS client: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates and configures an Amazon DynamoDB client
     *
     * @return Configured DynamoDB client
     */
    @Bean
    public AmazonDynamoDB dynamoDbClient() {
        try {
            return AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                    .withClientConfiguration(clientConfiguration())
                    .build();
        } catch (AmazonServiceException e) {
            // FIXME: Replace with proper logging and exception handling
            System.err.println("Error creating DynamoDB client: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates AWS credentials object from configured access key and secret key
     *
     * @return AWS credentials for client authentication
     */
    @Bean
    public AWSCredentials awsCredentials() {
        // TODO: Consider using a more secure way to handle credentials
        return new BasicAWSCredentials(accessKey, secretKey);
    }
    
    /**
     * Creates a client configuration with custom timeout and retry settings
     *
     * @return ClientConfiguration with custom settings
     */
    @Bean
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSocketTimeout(socketTimeout);
        clientConfig.setConnectionTimeout(connectionTimeout);
        clientConfig.setMaxErrorRetry(maxRetries);
        
        // Set additional client settings for better performance and reliability
        clientConfig.setUseGzip(true); // Enable GZIP compression to reduce bandwidth
        clientConfig.setUseTcpKeepAlive(true); // Keep connections alive
        
        // TODO: Add proxy configuration if needed in production environment
        // clientConfig.setProxyHost("proxy.example.com");
        // clientConfig.setProxyPort(8080);
        
        return clientConfig;
    }
}