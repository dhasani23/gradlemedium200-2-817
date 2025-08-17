package com.gradlemedium200.orderservice.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for DynamoDB setup and connection.
 * This class provides beans for DynamoDB client, credentials and mapper to be used throughout
 * the Order Service for accessing and manipulating DynamoDB tables.
 *
 * @author gradlemedium200
 * @version 1.0
 */
@Configuration
public class DynamoDbConfig {

    /**
     * AWS region for DynamoDB
     */
    @Value("${aws.dynamodb.region}")
    private String awsRegion;

    /**
     * AWS access key
     */
    @Value("${aws.accessKey}")
    private String accessKey;

    /**
     * AWS secret key
     */
    @Value("${aws.secretKey}")
    private String secretKey;

    /**
     * Creates AWS credentials for DynamoDB access.
     * 
     * @return AWSCredentials object with configured access and secret keys
     */
    @Bean
    public AWSCredentials awsCredentials() {
        // FIXME: Consider using IAM roles for production environments instead of static credentials
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    /**
     * Creates and configures DynamoDB client.
     * 
     * @return AmazonDynamoDB client configured with proper region and credentials
     */
    @Bean
    public AmazonDynamoDB dynamoDBClient() {
        try {
            // Convert string region to Regions enum value
            Regions regionEnum = Regions.fromName(awsRegion);
            
            return AmazonDynamoDBClientBuilder.standard()
                    .withRegion(regionEnum)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                    .build();
        } catch (IllegalArgumentException e) {
            // Handle case where region name is invalid
            throw new RuntimeException("Invalid AWS region specified: " + awsRegion, e);
        }
    }

    /**
     * Creates DynamoDB mapper with configured client.
     * 
     * @param dynamoDBClient the Amazon DynamoDB client
     * @return DynamoDBMapper configured with the provided client and default mapper config
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB dynamoDBClient) {
        // Create default mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withTableNameOverride(null)
                .build();

        // TODO: Add support for table name prefix based on environment
        return new DynamoDBMapper(dynamoDBClient, mapperConfig);
    }
}