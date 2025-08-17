package com.gradlemedium200.productcatalog.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.gradlemedium200.productcatalog.model.Category;
import com.gradlemedium200.productcatalog.model.Inventory;
import com.gradlemedium200.productcatalog.model.Product;
import com.gradlemedium200.productcatalog.model.ProductImage;
import com.gradlemedium200.productcatalog.model.Price;
import com.gradlemedium200.productcatalog.model.UserActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for DynamoDB connection and AWS SDK setup.
 * 
 * This class is responsible for setting up the AWS DynamoDB client and the DynamoDBMapper
 * used throughout the application for database operations. It also initializes the DynamoDB tables
 * if they don't exist when the application starts.
 */
@Configuration
public class DynamoDbConfig {
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbConfig.class);
    
    /**
     * AWS region for DynamoDB
     */
    @Value("${aws.dynamodb.region:us-east-1}")
    private String region;
    
    /**
     * DynamoDB endpoint URL
     */
    @Value("${aws.dynamodb.endpoint:}")
    private String endpoint;
    
    /**
     * AWS access key
     */
    @Value("${aws.accessKey:}")
    private String accessKey;
    
    /**
     * AWS secret key
     */
    @Value("${aws.secretKey:}")
    private String secretKey;

    /**
     * Create and configure DynamoDB client bean
     *
     * @return Configured AmazonDynamoDB client
     */
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        
        // If endpoint is provided, use it for local development
        if (endpoint != null && !endpoint.isEmpty()) {
            logger.info("Configuring DynamoDB with endpoint: {}", endpoint);
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, region)
            );
        } else {
            logger.info("Configuring DynamoDB with region: {}", region);
            builder.withRegion(region);
        }
        
        // If credentials are provided, use them
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            logger.info("Using provided AWS credentials");
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
        } else {
            // Otherwise, rely on default credential provider chain (instance profile, env variables, etc.)
            logger.info("Using default AWS credential provider chain");
        }
        
        return builder.build();
    }
    
    /**
     * Create DynamoDB mapper bean
     *
     * @param amazonDynamoDB Configured AmazonDynamoDB client
     * @return DynamoDBMapper instance
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        logger.info("Creating DynamoDBMapper");
        return new DynamoDBMapper(amazonDynamoDB);
    }
    
    /**
     * Initialize DynamoDB tables if they don't exist
     * This method runs after the beans are created and ensures all required tables exist
     */
    @PostConstruct
    public void createTablesIfNotExists() {
        logger.info("Checking if DynamoDB tables need to be created");
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDB();
        DynamoDBMapper dynamoDBMapper = dynamoDBMapper(amazonDynamoDB);
        
        // Get existing tables
        ListTablesResult listTablesResult = amazonDynamoDB.listTables();
        List<String> existingTables = listTablesResult.getTableNames();
        
        // Define model classes that need tables
        List<Class<?>> modelClasses = Arrays.asList(
            Product.class,
            Category.class, 
            Inventory.class,
            UserActivity.class,
            ProductImage.class,
            Price.class
        );
        
        // Create tables for each model if they don't exist
        for (Class<?> modelClass : modelClasses) {
            try {
                String tableName = modelClass.getSimpleName();
                
                // Check if table already exists
                if (existingTables.contains(tableName)) {
                    logger.info("Table '{}' already exists", tableName);
                    continue;
                }
                
                // Create table from entity model
                logger.info("Creating table '{}'", tableName);
                CreateTableRequest createTableRequest = dynamoDBMapper.generateCreateTableRequest(modelClass);
                
                // Configure provisioned throughput for the table
                createTableRequest.setProvisionedThroughput(
                    new ProvisionedThroughput(5L, 5L)
                );
                
                // Apply the request to create the table
                amazonDynamoDB.createTable(createTableRequest);
                
                logger.info("Created table '{}' successfully", tableName);
            } catch (Exception e) {
                logger.error("Error creating table for {}: {}", modelClass.getSimpleName(), e.getMessage());
                // Don't throw the exception to allow the application to start even if table creation fails
                // FIXME: Consider adding retry logic or alerting mechanism for failed table creation
            }
        }
    }
}