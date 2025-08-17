package com.gradlemedium200.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.gradlemedium200.dto.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Health checker for AWS services connectivity and status.
 * This service monitors AWS services like SNS, SQS, and DynamoDB to ensure they're operational.
 */
@Service
public class AwsHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(AwsHealthChecker.class);
    
    private final AmazonSNS amazonSNS;
    private final AmazonSQS amazonSQS;
    private final AmazonDynamoDB amazonDynamoDB;

    /**
     * Constructs an AwsHealthChecker with all required AWS clients.
     *
     * @param amazonSNS The Amazon SNS client
     * @param amazonSQS The Amazon SQS client
     * @param amazonDynamoDB The Amazon DynamoDB client
     */
    @Autowired
    public AwsHealthChecker(AmazonSNS amazonSNS, AmazonSQS amazonSQS, AmazonDynamoDB amazonDynamoDB) {
        this.amazonSNS = amazonSNS;
        this.amazonSQS = amazonSQS;
        this.amazonDynamoDB = amazonDynamoDB;
    }

    /**
     * Checks SNS service connectivity and health.
     *
     * @return HealthStatus representing the current state of the SNS service
     */
    public HealthStatus checkSnsHealth() {
        logger.debug("Checking SNS health");
        HealthStatus healthStatus = new HealthStatus();
        
        try {
            // Perform lightweight operation to check if SNS is accessible
            ListTopicsResult result = amazonSNS.listTopics(new ListTopicsRequest());
            int topicCount = result.getTopics() != null ? result.getTopics().size() : 0;
            
            healthStatus.addComponent("SNS", HealthStatus.Status.UP,
                    "SNS service is operational. Found " + topicCount + " topics.");
            healthStatus.setOverallStatus(HealthStatus.Status.UP);
        } catch (AmazonServiceException ase) {
            logger.error("Amazon service error while checking SNS health: {}", ase.getMessage(), ase);
            healthStatus.addComponent("SNS", HealthStatus.Status.DOWN,
                    "SNS service is down. Error: " + ase.getMessage() + " (Error code: " + ase.getErrorCode() + ")");
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        } catch (Exception e) {
            logger.error("Unexpected error while checking SNS health: {}", e.getMessage(), e);
            healthStatus.addComponent("SNS", HealthStatus.Status.DOWN, 
                    "SNS service check failed with unexpected error: " + e.getMessage());
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        }
        
        return healthStatus;
    }

    /**
     * Checks SQS service connectivity and health.
     *
     * @return HealthStatus representing the current state of the SQS service
     */
    public HealthStatus checkSqsHealth() {
        logger.debug("Checking SQS health");
        HealthStatus healthStatus = new HealthStatus();
        
        try {
            // Perform lightweight operation to check if SQS is accessible
            ListQueuesResult result = amazonSQS.listQueues(new ListQueuesRequest());
            int queueCount = result.getQueueUrls() != null ? result.getQueueUrls().size() : 0;
            
            healthStatus.addComponent("SQS", HealthStatus.Status.UP,
                    "SQS service is operational. Found " + queueCount + " queues.");
            healthStatus.setOverallStatus(HealthStatus.Status.UP);
        } catch (AmazonServiceException ase) {
            logger.error("Amazon service error while checking SQS health: {}", ase.getMessage(), ase);
            healthStatus.addComponent("SQS", HealthStatus.Status.DOWN,
                    "SQS service is down. Error: " + ase.getMessage() + " (Error code: " + ase.getErrorCode() + ")");
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        } catch (Exception e) {
            logger.error("Unexpected error while checking SQS health: {}", e.getMessage(), e);
            healthStatus.addComponent("SQS", HealthStatus.Status.DOWN, 
                    "SQS service check failed with unexpected error: " + e.getMessage());
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        }
        
        return healthStatus;
    }

    /**
     * Checks DynamoDB service connectivity and health.
     *
     * @return HealthStatus representing the current state of the DynamoDB service
     */
    public HealthStatus checkDynamoDbHealth() {
        logger.debug("Checking DynamoDB health");
        HealthStatus healthStatus = new HealthStatus();
        
        try {
            // Perform lightweight operation to check if DynamoDB is accessible
            ListTablesResult result = amazonDynamoDB.listTables(new ListTablesRequest().withLimit(10));
            int tableCount = result.getTableNames() != null ? result.getTableNames().size() : 0;
            
            healthStatus.addComponent("DynamoDB", HealthStatus.Status.UP,
                    "DynamoDB service is operational. Found " + tableCount + " tables.");
            healthStatus.setOverallStatus(HealthStatus.Status.UP);
        } catch (AmazonServiceException ase) {
            logger.error("Amazon service error while checking DynamoDB health: {}", ase.getMessage(), ase);
            healthStatus.addComponent("DynamoDB", HealthStatus.Status.DOWN,
                    "DynamoDB service is down. Error: " + ase.getMessage() + " (Error code: " + ase.getErrorCode() + ")");
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        } catch (Exception e) {
            logger.error("Unexpected error while checking DynamoDB health: {}", e.getMessage(), e);
            healthStatus.addComponent("DynamoDB", HealthStatus.Status.DOWN, 
                    "DynamoDB service check failed with unexpected error: " + e.getMessage());
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        }
        
        return healthStatus;
    }

    /**
     * Performs health check on all AWS services.
     *
     * @return Map containing service names as keys and their corresponding HealthStatus as values
     */
    public Map<String, HealthStatus> checkAllAwsServices() {
        logger.info("Performing health check on all AWS services");
        Map<String, HealthStatus> serviceHealthMap = new HashMap<>();
        
        // Check individual services
        HealthStatus snsHealth = checkSnsHealth();
        HealthStatus sqsHealth = checkSqsHealth();
        HealthStatus dynamoDbHealth = checkDynamoDbHealth();
        
        // Add results to the map
        serviceHealthMap.put("SNS", snsHealth);
        serviceHealthMap.put("SQS", sqsHealth);
        serviceHealthMap.put("DynamoDB", dynamoDbHealth);
        
        // Log results summary
        logHealthCheckSummary(serviceHealthMap);
        
        return serviceHealthMap;
    }
    
    /**
     * Logs a summary of all health checks for monitoring and debugging.
     * 
     * @param serviceHealthMap Map of service health statuses
     */
    private void logHealthCheckSummary(Map<String, HealthStatus> serviceHealthMap) {
        int healthyCount = 0;
        int degradedCount = 0;
        int downCount = 0;
        
        for (Map.Entry<String, HealthStatus> entry : serviceHealthMap.entrySet()) {
            HealthStatus status = entry.getValue();
            switch (status.getOverallStatus()) {
                case UP:
                    healthyCount++;
                    break;
                case DEGRADED:
                    degradedCount++;
                    break;
                case DOWN:
                    downCount++;
                    logger.warn("Service {} is DOWN", entry.getKey());
                    break;
                default:
                    // Unknown status, do nothing
                    break;
            }
        }
        
        if (downCount > 0) {
            logger.warn("AWS Health Check Summary: {} healthy, {} degraded, {} down services", 
                    healthyCount, degradedCount, downCount);
        } else if (degradedCount > 0) {
            logger.info("AWS Health Check Summary: {} healthy, {} degraded, {} down services", 
                    healthyCount, degradedCount, downCount);
        } else {
            logger.info("All AWS services are healthy: {} services checked", healthyCount);
        }
    }
    
    /**
     * Performs a comprehensive health check on all AWS services and returns a single HealthStatus object.
     *
     * @return HealthStatus representing the overall health status of all AWS services
     */
    public HealthStatus checkAllServices() {
        logger.info("Performing comprehensive health check on all AWS services");
        HealthStatus healthStatus = new HealthStatus();
        
        try {
            // Check SNS
            HealthStatus snsHealth = checkSnsHealth();
            healthStatus.addComponent("SNS", snsHealth.getOverallStatus(), 
                    snsHealth.getComponents().get("SNS").getDetails());
            
            // Check SQS
            HealthStatus sqsHealth = checkSqsHealth();
            healthStatus.addComponent("SQS", sqsHealth.getOverallStatus(),
                    sqsHealth.getComponents().get("SQS").getDetails());
            
            // Check DynamoDB
            HealthStatus dynamoDbHealth = checkDynamoDbHealth();
            healthStatus.addComponent("DynamoDB", dynamoDbHealth.getOverallStatus(),
                    dynamoDbHealth.getComponents().get("DynamoDB").getDetails());
            
            // Update overall status based on all component statuses
            healthStatus.updateOverallStatus();
            
        } catch (Exception e) {
            logger.error("Unexpected error during comprehensive AWS health check: {}", e.getMessage(), e);
            healthStatus.addComponent("AWS", HealthStatus.Status.DOWN, 
                    "AWS health check failed with unexpected error: " + e.getMessage());
            healthStatus.setOverallStatus(HealthStatus.Status.DOWN);
        }
        
        return healthStatus;
    }
    
    /**
     * TODO: Add more detailed metrics collection for AWS service performance
     * This could include response time measurements, throttling metrics, etc.
     */
    
    /**
     * FIXME: The current implementation doesn't handle regional failovers.
     * Need to implement cross-region health checking for disaster recovery scenarios.
     */
}