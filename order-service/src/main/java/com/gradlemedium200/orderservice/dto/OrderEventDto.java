package com.gradlemedium200.orderservice.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object for order events in SQS messages.
 * This class is used to transport order event information between services.
 * It contains event metadata and order data relevant for asynchronous processing.
 */
public class OrderEventDto {
    
    // Event metadata fields
    private String eventId;
    private String orderId;
    private String customerId;
    private String eventType;
    private Map<String, Object> eventData;
    private LocalDateTime timestamp;

    /**
     * Default constructor that initializes an empty event DTO
     * with a generated event ID and current timestamp.
     */
    public OrderEventDto() {
        this.eventId = UUID.randomUUID().toString();
        this.eventData = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with all required fields.
     * 
     * @param eventId Unique identifier for this event
     * @param orderId Order identifier this event relates to
     * @param customerId Customer identifier associated with the order
     * @param eventType Type of event (CREATED, UPDATED, CANCELLED, etc.)
     * @param eventData Additional event data as key-value pairs
     * @param timestamp Timestamp when the event was created
     */
    public OrderEventDto(String eventId, String orderId, String customerId, String eventType, 
                         Map<String, Object> eventData, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.eventType = eventType;
        this.eventData = eventData != null ? eventData : new HashMap<>();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    /**
     * Validates that this event object contains all required data and is suitable for processing.
     * 
     * @return true if the event is valid, false otherwise
     */
    public boolean validate() {
        // Basic validation rules
        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }
        
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        
        if (customerId == null || customerId.trim().isEmpty()) {
            return false;
        }
        
        if (eventType == null || eventType.trim().isEmpty()) {
            return false;
        }
        
        // Ensure we have a timestamp
        if (timestamp == null) {
            return false;
        }
        
        // Additional validation based on event type
        if (eventType.equals("CREATED") || eventType.equals("UPDATED")) {
            // These events should have at least some data
            return eventData != null && !eventData.isEmpty();
        } else if (eventType.equals("CANCELLED")) {
            // Cancelled events should have a reason
            return eventData != null && eventData.containsKey("reason");
        }
        
        // Default case - basic validation passed
        return true;
    }

    /**
     * Adds a key-value pair to the event data
     * 
     * @param key The key for the data entry
     * @param value The value for the data entry
     * @return this OrderEventDto instance for method chaining
     */
    public OrderEventDto addData(String key, Object value) {
        if (this.eventData == null) {
            this.eventData = new HashMap<>();
        }
        this.eventData.put(key, value);
        return this;
    }

    /**
     * Factory method to create a new order created event.
     * 
     * @param orderId The ID of the created order
     * @param customerId The customer ID associated with the order
     * @param orderData Map containing order details
     * @return A new OrderEventDto configured as a creation event
     */
    public static OrderEventDto createOrderCreatedEvent(String orderId, String customerId, Map<String, Object> orderData) {
        OrderEventDto event = new OrderEventDto();
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setEventType("CREATED");
        event.setEventData(orderData);
        return event;
    }

    // Getters and Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, Object> eventData) {
        this.eventData = eventData != null ? eventData : new HashMap<>();
    }

    /**
     * Convenience method to get the new status from event data.
     * 
     * @return The new status or null if not present
     */
    public String getNewStatus() {
        if (this.eventData != null && this.eventData.containsKey("newStatus")) {
            return this.eventData.get("newStatus").toString();
        }
        return null;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OrderEventDto that = (OrderEventDto) o;
        
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(orderId, that.orderId) &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, orderId, customerId, eventType, timestamp);
    }

    @Override
    public String toString() {
        return "OrderEventDto{" +
                "eventId='" + eventId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", eventData.size=" + (eventData != null ? eventData.size() : 0) +
                '}';
    }
}