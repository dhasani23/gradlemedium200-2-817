package com.gradlemedium200.health;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for monitoring DynamoDB connectivity.
 * This indicator performs health checks on DynamoDB by listing tables
 * and measuring response latency.
 */
@Component
public class DynamoDbHealthIndicator implements HealthIndicator {

    private final AmazonDynamoDB amazonDynamoDB;
    private final String testTableName;

    /**
     * Constructor for the DynamoDB health indicator.
     *
     * @param amazonDynamoDB DynamoDB client for health checks
     * @param testTableName Name of test table for health checks
     */
    @Autowired
    public DynamoDbHealthIndicator(AmazonDynamoDB amazonDynamoDB,
                                  @Value("${aws.dynamodb.test-table:health-test-table}") String testTableName) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.testTableName = testTableName;
    }

    /**
     * Performs DynamoDB health check and returns status.
     * 
     * @return Health object containing status and additional details
     */
    @Override
    public Health health() {
        try {
            boolean isConnected = checkDynamoDbConnection();
            if (!isConnected) {
                return Health.down()
                        .withDetail("error", "Unable to connect to DynamoDB")
                        .build();
            }
            
            long latency = measureLatency();
            
            // Define thresholds for response time
            if (latency > 1000) {
                return Health.status("DEGRADED")
                        .withDetail("message", "DynamoDB connection is slow")
                        .withDetail("responseTime", latency + "ms")
                        .build();
            }
            
            return Health.up()
                    .withDetail("message", "DynamoDB connection is healthy")
                    .withDetail("responseTime", latency + "ms")
                    .withDetail("testTable", testTableName)
                    .build();
            
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Error checking DynamoDB health: " + e.getMessage())
                    .withDetail("exception", e.getClass().getName())
                    .build();
        }
    }
    
    /**
     * Checks DynamoDB connection by listing tables.
     * 
     * @return true if connection is successful, false otherwise
     */
    boolean checkDynamoDbConnection() {
        try {
            // Attempt to list tables to verify connectivity
            ListTablesRequest request = new ListTablesRequest().withLimit(1);
            ListTablesResult result = amazonDynamoDB.listTables(request);
            
            // Additionally check if the test table is present
            // FIXME: This may cause the health check to fail if the test table doesn't exist
            //        Consider making this check optional or creating the table if missing
            return result != null && 
                   (testTableName == null || result.getTableNames().contains(testTableName));
                   
        } catch (Exception e) {
            // Log the exception details
            // TODO: Add proper logging using SLF4J
            return false;
        }
    }
    
    /**
     * Measures DynamoDB response latency.
     * 
     * @return response time in milliseconds
     */
    long measureLatency() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Make a lightweight request to measure latency
            ListTablesRequest request = new ListTablesRequest().withLimit(1);
            amazonDynamoDB.listTables(request);
            
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            // In case of failure, return a high latency value to indicate issues
            // TODO: Add proper logging of the exception
            return Long.MAX_VALUE;
        }
    }
}