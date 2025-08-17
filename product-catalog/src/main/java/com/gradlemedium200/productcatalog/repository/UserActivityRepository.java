package com.gradlemedium200.productcatalog.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.productcatalog.model.UserActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for tracking user activity data for recommendation engine using DynamoDB.
 * 
 * This repository manages the persistence and retrieval of user activity data that is used
 * by the recommendation engine to generate personalized product recommendations.
 */
@Repository
public class UserActivityRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserActivityRepository.class);
    
    /**
     * DynamoDB mapper for user activity operations
     */
    private final DynamoDBMapper dynamoDBMapper;
    
    /**
     * Constructor with DynamoDBMapper dependency injection
     *
     * @param dynamoDBMapper DynamoDB mapper for user activity operations
     */
    @Autowired
    public UserActivityRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }
    
    /**
     * Save user activity record
     *
     * @param activity The user activity to save
     * @return The saved user activity with generated ID
     */
    public UserActivity save(UserActivity activity) {
        try {
            logger.debug("Saving user activity: {}", activity);
            
            // Generate an ID if one is not provided
            if (activity.getActivityId() == null || activity.getActivityId().isEmpty()) {
                activity.setActivityId(UUID.randomUUID().toString());
            }
            
            // Set timestamp if not already set
            if (activity.getTimestamp() == null) {
                activity.setTimestamp(LocalDateTime.now());
            }
            
            dynamoDBMapper.save(activity);
            logger.debug("Successfully saved user activity with ID: {}", activity.getActivityId());
            return activity;
        } catch (Exception e) {
            logger.error("Error saving user activity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user activity", e);
        }
    }
    
    /**
     * Find activities by user ID
     *
     * @param userId The ID of the user
     * @return List of user activities for the specified user
     */
    public List<UserActivity> findByUserId(String userId) {
        try {
            logger.debug("Finding activities for user ID: {}", userId);
            
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":userId", new AttributeValue().withS(userId));
            
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :userId")
                .withExpressionAttributeValues(eav);
            
            List<UserActivity> results = dynamoDBMapper.scan(UserActivity.class, scanExpression);
            logger.debug("Found {} activities for user ID: {}", results.size(), userId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding activities by user ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find activities by user ID", e);
        }
    }
    
    /**
     * Find activities for a specific product
     *
     * @param productId The ID of the product
     * @return List of user activities related to the specified product
     */
    public List<UserActivity> findByProductId(String productId) {
        try {
            logger.debug("Finding activities for product ID: {}", productId);
            
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":productId", new AttributeValue().withS(productId));
            
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("productId = :productId")
                .withExpressionAttributeValues(eav);
            
            List<UserActivity> results = dynamoDBMapper.scan(UserActivity.class, scanExpression);
            logger.debug("Found {} activities for product ID: {}", results.size(), productId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding activities by product ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find activities by product ID", e);
        }
    }
    
    /**
     * Find recent activities for a user within specified hours
     *
     * @param userId The ID of the user
     * @param hours The number of hours to look back
     * @return List of recent user activities within the specified time frame
     */
    public List<UserActivity> findRecentActivities(String userId, int hours) {
        try {
            logger.debug("Finding recent activities for user ID: {} in the last {} hours", userId, hours);
            
            LocalDateTime cutoffTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
            // TODO: Implement proper timestamp conversion for DynamoDB comparison
            // This is a simplified implementation that requires post-filtering
            
            // First get all activities for the user
            List<UserActivity> allUserActivities = findByUserId(userId);
            
            // Then filter by timestamp in memory
            // FIXME: This is inefficient for large datasets and should be replaced with a proper query
            allUserActivities.removeIf(activity -> 
                activity.getTimestamp() == null || activity.getTimestamp().isBefore(cutoffTime));
            
            logger.debug("Found {} recent activities for user ID: {} in the last {} hours", 
                allUserActivities.size(), userId, hours);
            return allUserActivities;
        } catch (Exception e) {
            logger.error("Error finding recent activities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find recent activities", e);
        }
    }
    
    /**
     * Find activities by type
     *
     * @param activityType The type of activity (e.g., VIEW, PURCHASE, etc.)
     * @return List of user activities of the specified type
     */
    public List<UserActivity> findByActivityType(String activityType) {
        try {
            logger.debug("Finding activities of type: {}", activityType);
            
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":activityType", new AttributeValue().withS(activityType));
            
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("activityType = :activityType")
                .withExpressionAttributeValues(eav);
            
            List<UserActivity> results = dynamoDBMapper.scan(UserActivity.class, scanExpression);
            logger.debug("Found {} activities of type: {}", results.size(), activityType);
            return results;
        } catch (Exception e) {
            logger.error("Error finding activities by type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find activities by type", e);
        }
    }
    
    /**
     * Advanced find method to get user activities by multiple criteria
     * 
     * @param userId The ID of the user (optional)
     * @param productId The ID of the product (optional)
     * @param activityType The type of activity (optional)
     * @param startTime The start time for the search period (optional)
     * @param endTime The end time for the search period (optional)
     * @return List of user activities matching the criteria
     */
    public List<UserActivity> findByMultipleCriteria(
            String userId, 
            String productId, 
            String activityType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        try {
            logger.debug("Finding activities with multiple criteria");
            
            Map<String, AttributeValue> eav = new HashMap<>();
            StringBuilder filterExpression = new StringBuilder();
            
            // Build dynamic filter expression based on provided criteria
            if (userId != null && !userId.isEmpty()) {
                eav.put(":userId", new AttributeValue().withS(userId));
                addConditionToFilter(filterExpression, "userId = :userId");
            }
            
            if (productId != null && !productId.isEmpty()) {
                eav.put(":productId", new AttributeValue().withS(productId));
                addConditionToFilter(filterExpression, "productId = :productId");
            }
            
            if (activityType != null && !activityType.isEmpty()) {
                eav.put(":activityType", new AttributeValue().withS(activityType));
                addConditionToFilter(filterExpression, "activityType = :activityType");
            }
            
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            if (!eav.isEmpty()) {
                scanExpression.withFilterExpression(filterExpression.toString())
                             .withExpressionAttributeValues(eav);
            }
            
            List<UserActivity> results = dynamoDBMapper.scan(UserActivity.class, scanExpression);
            
            // Filter by time range in memory
            // TODO: Implement proper timestamp querying in DynamoDB
            if (startTime != null || endTime != null) {
                results.removeIf(activity -> {
                    if (activity.getTimestamp() == null) {
                        return true;
                    }
                    boolean afterStart = startTime == null || !activity.getTimestamp().isBefore(startTime);
                    boolean beforeEnd = endTime == null || !activity.getTimestamp().isAfter(endTime);
                    return !(afterStart && beforeEnd);
                });
            }
            
            logger.debug("Found {} activities matching criteria", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding activities by multiple criteria: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find activities by multiple criteria", e);
        }
    }
    
    /**
     * Helper method to add conditions to the filter expression with proper AND operators
     */
    private void addConditionToFilter(StringBuilder filterExpression, String condition) {
        if (filterExpression.length() > 0) {
            filterExpression.append(" AND ");
        }
        filterExpression.append(condition);
    }
}