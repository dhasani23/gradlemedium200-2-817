package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.gradlemedium200.productcatalog.util.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity model for tracking user interactions with products for recommendation engine.
 * This class represents different types of user activities like viewing, purchasing,
 * adding to cart, etc. that are tracked for generating personalized recommendations.
 */
@DynamoDBTable(tableName = "UserActivity")
public class UserActivity {
    
    // Activity types
    public static final String ACTIVITY_TYPE_VIEW = "VIEW";
    public static final String ACTIVITY_TYPE_PURCHASE = "PURCHASE";
    public static final String ACTIVITY_TYPE_ADD_TO_CART = "ADD_TO_CART";
    public static final String ACTIVITY_TYPE_WISHLIST = "WISHLIST";
    public static final String ACTIVITY_TYPE_SEARCH = "SEARCH";
    
    // Device types
    public static final String DEVICE_TYPE_MOBILE = "MOBILE";
    public static final String DEVICE_TYPE_DESKTOP = "DESKTOP";
    public static final String DEVICE_TYPE_TABLET = "TABLET";
    
    private String activityId;
    private String userId;
    private String productId;
    private String activityType;
    private String categoryId;
    private String sessionId;
    private String deviceType;
    private LocalDateTime timestamp;
    private Map<String, String> metadata;
    
    /**
     * Default constructor
     */
    public UserActivity() {
        this.metadata = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param userId The ID of the user who performed the activity
     * @param productId The ID of the product involved in the activity
     * @param activityType The type of activity performed
     */
    public UserActivity(String userId, String productId, String activityType) {
        this();
        this.userId = userId;
        this.productId = productId;
        this.activityType = activityType;
    }
    
    /**
     * Get the unique identifier for this activity record
     * 
     * @return The activity ID
     */
    @DynamoDBHashKey(attributeName = "activityId")
    public String getActivityId() {
        return activityId;
    }
    
    /**
     * Set the unique identifier for this activity record
     * 
     * @param activityId The activity ID to set
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    
    /**
     * Get the ID of the user who performed this activity
     * 
     * @return The user ID
     */
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }
    
    /**
     * Set the ID of the user who performed this activity
     * 
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Get the ID of the product involved in this activity
     * 
     * @return The product ID
     */
    @DynamoDBAttribute(attributeName = "productId")
    public String getProductId() {
        return productId;
    }
    
    /**
     * Set the ID of the product involved in this activity
     * 
     * @param productId The product ID to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    /**
     * Get the type of activity performed
     * 
     * @return The activity type
     */
    @DynamoDBAttribute(attributeName = "activityType")
    public String getActivityType() {
        return activityType;
    }
    
    /**
     * Set the type of activity performed
     * 
     * @param activityType The activity type to set
     */
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    /**
     * Get the ID of the category of the product for activity tracking
     * 
     * @return The category ID
     */
    @DynamoDBAttribute(attributeName = "categoryId")
    public String getCategoryId() {
        return categoryId;
    }
    
    /**
     * Set the ID of the category of the product for activity tracking
     * 
     * @param categoryId The category ID to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * Get the user session ID when the activity occurred
     * 
     * @return The session ID
     */
    @DynamoDBAttribute(attributeName = "sessionId")
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Set the user session ID when the activity occurred
     * 
     * @param sessionId The session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Get the type of device used for this activity
     * 
     * @return The device type
     */
    @DynamoDBAttribute(attributeName = "deviceType")
    public String getDeviceType() {
        return deviceType;
    }
    
    /**
     * Set the type of device used for this activity
     * 
     * @param deviceType The device type to set
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    /**
     * Get the timestamp when the activity occurred
     * 
     * @return The activity timestamp
     */
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    @DynamoDBAttribute(attributeName = "timestamp")
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp when the activity occurred
     * 
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get additional metadata about the activity
     * 
     * @return Map of metadata key-value pairs
     */
    @DynamoDBAttribute(attributeName = "metadata")
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Set additional metadata about the activity
     * 
     * @param metadata Map of metadata key-value pairs
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Adds a single metadata entry to the metadata map
     * 
     * @param key The metadata key
     * @param value The metadata value
     * @return This UserActivity instance for method chaining
     */
    public UserActivity addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Check if this activity occurred within the specified number of hours from now
     * 
     * @param hours The number of hours to check against
     * @return true if the activity occurred within the specified hours, false otherwise
     */
    public boolean isRecentActivity(int hours) {
        if (timestamp == null) {
            return false;
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return timestamp.isAfter(cutoffTime);
    }
    
    @Override
    public String toString() {
        return "UserActivity{" +
                "activityId='" + activityId + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", activityType='" + activityType + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", timestamp=" + timestamp +
                ", metadata=" + metadata +
                '}';
    }
    
    /**
     * Custom converter for LocalDateTime to be stored in DynamoDB
     * 
     * @deprecated Use {@link com.gradlemedium200.productcatalog.util.LocalDateTimeConverter} instead
     */
    @Deprecated
    public static class LocalDateTimeConverter extends com.gradlemedium200.productcatalog.util.LocalDateTimeConverter {
        // This class extends the common converter for backward compatibility
    }
}