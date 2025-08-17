package com.gradlemedium200.productcatalog.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for AWS services including SNS, SQS, and S3.
 * This class provides beans for connecting to various AWS services used 
 * by the product catalog module.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.accessKey}")
    private String accessKey;
    
    @Value("${aws.secretKey}")
    private String secretKey;
    
    @Value("${aws.sns.topicArn}")
    private String snsTopicArn;
    
    @Value("${aws.sqs.queueUrl}")
    private String sqsQueueUrl;
    
    @Value("${aws.s3.bucketName}")
    private String s3BucketName;

    /**
     * Creates and configures an Amazon SNS client.
     * 
     * @return configured AmazonSNS client
     */
    @Bean
    public AmazonSNS amazonSNS() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        // Create SNS client with region and credentials
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
                
        // Log successful client creation
        try {
            // Validate that we can access the configured topic
            snsClient.listSubscriptionsByTopic(snsTopicArn);
            // FIXME: Add proper logging instead of printing to console
            System.out.println("Successfully connected to SNS topic: " + snsTopicArn);
        } catch (Exception e) {
            // TODO: Implement proper error handling with retries
            System.err.println("Error connecting to SNS: " + e.getMessage());
        }
        
        return snsClient;
    }

    /**
     * Creates and configures an Amazon SQS client.
     * 
     * @return configured AmazonSQS client
     */
    @Bean
    public AmazonSQS amazonSQS() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        // Build SQS client with credentials and region
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
        
        // Validate queue exists and is accessible
        try {
            sqsClient.getQueueUrl(sqsQueueUrl.substring(sqsQueueUrl.lastIndexOf("/") + 1));
            // FIXME: Add proper logging instead of printing to console
            System.out.println("Successfully connected to SQS queue: " + sqsQueueUrl);
        } catch (Exception e) {
            // TODO: Implement proper error handling strategy
            System.err.println("Error connecting to SQS queue: " + e.getMessage());
        }
        
        return sqsClient;
    }

    /**
     * Creates and configures an Amazon S3 client.
     * 
     * @return configured AmazonS3 client
     */
    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        // Build S3 client with credentials and region
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
        
        // Verify bucket exists and is accessible
        try {
            if (!s3Client.doesBucketExistV2(s3BucketName)) {
                // TODO: Consider auto-creation of bucket if not exists
                System.err.println("Warning: S3 bucket does not exist: " + s3BucketName);
            } else {
                System.out.println("Successfully connected to S3 bucket: " + s3BucketName);
            }
        } catch (Exception e) {
            // TODO: Add proper exception handling and recovery strategy
            System.err.println("Error connecting to S3: " + e.getMessage());
        }
        
        return s3Client;
    }

    // Getters for configuration values
    
    public String getRegion() {
        return region;
    }

    public String getSnsTopicArn() {
        return snsTopicArn;
    }

    public String getSqsQueueUrl() {
        return sqsQueueUrl;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }
    
    // Note: Not exposing access and secret keys through getters for security reasons
}