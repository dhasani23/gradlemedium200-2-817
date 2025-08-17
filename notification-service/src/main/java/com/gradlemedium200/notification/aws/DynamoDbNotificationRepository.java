package com.gradlemedium200.notification.aws;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;
import com.gradlemedium200.notification.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DynamoDB repository implementation for storing notification metadata and delivery status
 * with high availability and scalability.
 * 
 * This implementation leverages DynamoDB's document interface to store and retrieve
 * notification data, and uses Global Secondary Indexes (GSIs) for efficient querying
 * by recipient and status.
 */
@Repository
public class DynamoDbNotificationRepository implements NotificationRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbNotificationRepository.class);
    private static final String ID_ATTRIBUTE = "id";
    private static final String RECIPIENT_ID_ATTRIBUTE = "recipientId";
    private static final String STATUS_ATTRIBUTE = "status";
    private static final String GSI_RECIPIENT_INDEX = "recipientId-index";
    private static final String GSI_STATUS_INDEX = "status-index";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * DynamoDB document interface
     */
    private final DynamoDB dynamoDB;
    
    /**
     * DynamoDB table for notifications
     */
    private final Table table;
    
    /**
     * Name of the DynamoDB table
     */
    private final String tableName;
    
    /**
     * Jackson object mapper for JSON serialization
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructor with DynamoDB and table name injection.
     *
     * @param dynamoDB The DynamoDB client
     * @param tableName The name of the notifications table
     * @param objectMapper The Jackson ObjectMapper for JSON serialization
     */
    @Autowired
    public DynamoDbNotificationRepository(
            DynamoDB dynamoDB,
            @Value("${aws.dynamodb.notifications.table-name}") String tableName,
            ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
        this.table = dynamoDB.getTable(tableName);
        this.objectMapper = objectMapper;
        
        logger.info("Initialized DynamoDbNotificationRepository with table: {}", tableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Notification save(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        
        // Generate ID if not already set
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        
        // Ensure createdAt is set
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        
        try {
            Map<String, Object> itemMap = convertToItem(notification);
            Item item = Item.fromMap(itemMap);
            
            logger.debug("Saving notification with ID: {}", notification.getId());
            table.putItem(item);
            
            return notification;
        } catch (Exception e) {
            logger.error("Error saving notification to DynamoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save notification to DynamoDB", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Notification> findById(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        try {
            Item item = table.getItem(ID_ATTRIBUTE, id);
            if (item == null) {
                logger.debug("Notification not found with ID: {}", id);
                return Optional.empty();
            }
            
            Notification notification = convertFromItem(item.asMap());
            return Optional.of(notification);
        } catch (Exception e) {
            logger.error("Error retrieving notification from DynamoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve notification from DynamoDB", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        if (recipientId == null || recipientId.isEmpty()) {
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        try {
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("recipientId = :recipientId")
                    .withValueMap(new ValueMap().withString(":recipientId", recipientId));
            
            ItemCollection<QueryOutcome> items = table.getIndex(GSI_RECIPIENT_INDEX).query(querySpec);
            
            List<Notification> notifications = new ArrayList<>();
            Iterator<Item> iterator = items.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                notifications.add(convertFromItem(item.asMap()));
            }
            
            logger.debug("Found {} notifications for recipient ID: {}", notifications.size(), recipientId);
            return notifications;
        } catch (Exception e) {
            logger.error("Error finding notifications by recipient ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find notifications by recipient ID", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        try {
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("status = :status")
                    .withValueMap(new ValueMap().withString(":status", status.name()));
            
            ItemCollection<QueryOutcome> items = table.getIndex(GSI_STATUS_INDEX).query(querySpec);
            
            List<Notification> notifications = new ArrayList<>();
            Iterator<Item> iterator = items.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                notifications.add(convertFromItem(item.asMap()));
            }
            
            logger.debug("Found {} notifications with status: {}", notifications.size(), status);
            return notifications;
        } catch (Exception e) {
            logger.error("Error finding notifications by status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find notifications by status", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(String id, NotificationStatus status) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        try {
            // Get current item to update
            Optional<Notification> existingNotification = findById(id);
            if (!existingNotification.isPresent()) {
                throw new IllegalArgumentException("Notification not found with ID: " + id);
            }
            
            Notification notification = existingNotification.get();
            notification.setStatus(status);
            
            // For SENT status, update sentAt timestamp
            if (status == NotificationStatus.SENT) {
                notification.setSentAt(LocalDateTime.now());
            }
            
            // Update the item in DynamoDB
            save(notification);
            
            logger.debug("Updated notification status to {} for ID: {}", status, id);
        } catch (ConditionalCheckFailedException e) {
            logger.error("Notification with ID {} does not exist", id);
            throw new IllegalArgumentException("Notification not found with ID: " + id, e);
        } catch (Exception e) {
            logger.error("Error updating notification status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update notification status", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        try {
            table.deleteItem(ID_ATTRIBUTE, id);
            logger.debug("Deleted notification with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete notification", e);
        }
    }

    /**
     * Batch saves multiple notifications to DynamoDB.
     * This method optimizes the saving of multiple notifications by using
     * DynamoDB's batch write capabilities.
     *
     * @param notifications The list of notifications to save
     * @throws IllegalArgumentException if the notifications list is null
     */
    public void batchSave(List<Notification> notifications) {
        if (notifications == null) {
            throw new IllegalArgumentException("Notifications list cannot be null");
        }
        
        if (notifications.isEmpty()) {
            logger.debug("No notifications to batch save");
            return;
        }
        
        try {
            // Generate IDs for notifications without one
            List<Item> items = notifications.stream()
                .map(notification -> {
                    // Generate ID if not already set
                    if (notification.getId() == null || notification.getId().isEmpty()) {
                        notification.setId(UUID.randomUUID().toString());
                    }
                    
                    // Ensure createdAt is set
                    if (notification.getCreatedAt() == null) {
                        notification.setCreatedAt(LocalDateTime.now());
                    }
                    
                    return Item.fromMap(convertToItem(notification));
                })
                .collect(Collectors.toList());
            
            // TODO: Implement proper batching to handle DynamoDB's limit of 25 items per batch write
            // FIXME: Current implementation doesn't use batch write API, optimize this for production
            
            // For now, individually save each item
            for (Item item : items) {
                table.putItem(item);
            }
            
            logger.debug("Batch saved {} notifications", notifications.size());
        } catch (Exception e) {
            logger.error("Error batch saving notifications: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch save notifications", e);
        }
    }

    /**
     * Converts a Notification object to a DynamoDB item map.
     *
     * @param notification The notification to convert
     * @return Map representing the DynamoDB item
     */
    public Map<String, Object> convertToItem(Notification notification) {
        Map<String, Object> item = new HashMap<>();
        
        // Add all basic notification fields
        item.put(ID_ATTRIBUTE, notification.getId());
        item.put(RECIPIENT_ID_ATTRIBUTE, notification.getRecipientId());
        item.put("message", notification.getMessage());
        item.put("subject", notification.getSubject());
        item.put(STATUS_ATTRIBUTE, notification.getStatus().name());
        item.put("type", notification.getType().name());
        item.put("retryCount", notification.getRetryCount());
        
        // Format date-time fields
        if (notification.getCreatedAt() != null) {
            item.put("createdAt", notification.getCreatedAt().format(DATE_FORMATTER));
        }
        
        if (notification.getSentAt() != null) {
            item.put("sentAt", notification.getSentAt().format(DATE_FORMATTER));
        }
        
        return item;
    }

    /**
     * Converts a DynamoDB item map to a Notification object.
     *
     * @param item The DynamoDB item map
     * @return Notification object created from the map
     */
    public Notification convertFromItem(Map<String, Object> item) {
        Notification notification = new Notification();
        
        notification.setId((String) item.get(ID_ATTRIBUTE));
        notification.setRecipientId((String) item.get(RECIPIENT_ID_ATTRIBUTE));
        notification.setMessage((String) item.get("message"));
        notification.setSubject((String) item.get("subject"));
        
        // Enum conversions
        if (item.containsKey(STATUS_ATTRIBUTE)) {
            notification.setStatus(NotificationStatus.valueOf((String) item.get(STATUS_ATTRIBUTE)));
        }
        
        // Parse dates
        String createdAtStr = (String) item.get("createdAt");
        if (createdAtStr != null) {
            notification.setCreatedAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER));
        }
        
        String sentAtStr = (String) item.get("sentAt");
        if (sentAtStr != null) {
            notification.setSentAt(LocalDateTime.parse(sentAtStr, DATE_FORMATTER));
        }
        
        // Numeric values
        if (item.containsKey("retryCount")) {
            Object retryCountObj = item.get("retryCount");
            if (retryCountObj instanceof Number) {
                notification.setRetryCount(((Number) retryCountObj).intValue());
            }
        }
        
        return notification;
    }
}