package com.gradlemedium200.orderservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.orderservice.model.Order;
import com.gradlemedium200.orderservice.model.OrderStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for order persistence operations using DynamoDB.
 * Handles CRUD operations for Order entities in Amazon DynamoDB.
 */
@Repository
public class OrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    
    private final DynamoDBMapper dynamoDbTemplate;

    /**
     * Constructs a new OrderRepository with the provided DynamoDB mapper.
     *
     * @param dynamoDbTemplate DynamoDB mapper for data operations
     */
    @Autowired
    public OrderRepository(DynamoDBMapper dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    /**
     * Saves an order to DynamoDB.
     *
     * @param order The order entity to save
     * @return The saved order with any generated values
     */
    public Order save(Order order) {
        try {
            logger.debug("Saving order with ID: {}", order.getOrderId());
            dynamoDbTemplate.save(order);
            return order;
        } catch (Exception e) {
            logger.error("Error saving order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save order", e);
        }
    }

    /**
     * Finds an order by its ID.
     *
     * @param orderId The ID of the order to find
     * @return An Optional containing the order if found, or empty if not found
     */
    public Optional<Order> findById(String orderId) {
        try {
            logger.debug("Finding order with ID: {}", orderId);
            Order order = dynamoDbTemplate.load(Order.class, orderId);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            logger.error("Error finding order by ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to find order by ID: " + orderId, e);
        }
    }

    /**
     * Finds all orders for a specific customer.
     *
     * @param customerId The customer ID to find orders for
     * @return A list of orders for the customer
     */
    public List<Order> findByCustomerId(String customerId) {
        try {
            logger.debug("Finding orders for customer ID: {}", customerId);
            
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":customerId", new AttributeValue().withS(customerId));
            
            // Create a query expression to find orders by customer ID
            // Note: This assumes there's a GSI on customerId
            DynamoDBQueryExpression<Order> queryExpression = new DynamoDBQueryExpression<Order>()
                .withIndexName("customerIdIndex")  // The GSI name on customerId
                .withConsistentRead(false)  // GSI queries cannot use consistent reads
                .withKeyConditionExpression("customerId = :customerId")
                .withExpressionAttributeValues(eav);
                
            return dynamoDbTemplate.query(Order.class, queryExpression);
        } catch (Exception e) {
            logger.error("Error finding orders for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to find orders for customer: " + customerId, e);
        }
    }

    /**
     * Finds orders by their status.
     *
     * @param status The order status to find
     * @return A list of orders with the specified status
     */
    public List<Order> findByStatus(OrderStatus status) {
        try {
            logger.debug("Finding orders with status: {}", status);
            
            // For production environments, you would typically use a GSI on orderStatus
            // Here, we're using a scan with a filter expression as a fallback approach
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":status", new AttributeValue().withS(status.name()));
            
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("orderStatus = :status")
                .withExpressionAttributeValues(eav);
                
            return dynamoDbTemplate.scan(Order.class, scanExpression);
            
            // TODO: Replace scan with a query using a GSI on orderStatus for better performance
        } catch (Exception e) {
            logger.error("Error finding orders with status {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Failed to find orders by status: " + status, e);
        }
    }

    /**
     * Deletes an order by its ID.
     *
     * @param orderId The ID of the order to delete
     */
    public void delete(String orderId) {
        try {
            logger.debug("Deleting order with ID: {}", orderId);
            
            // First, load the order to ensure it exists
            Optional<Order> orderOpt = findById(orderId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                dynamoDbTemplate.delete(order);
                logger.info("Order with ID {} has been deleted", orderId);
            } else {
                logger.warn("Attempted to delete non-existent order with ID: {}", orderId);
            }
        } catch (Exception e) {
            logger.error("Error deleting order with ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete order: " + orderId, e);
        }
    }

    /**
     * Checks if an order exists by its ID.
     *
     * @param orderId The ID of the order to check
     * @return true if the order exists, false otherwise
     */
    public boolean existsById(String orderId) {
        try {
            logger.debug("Checking if order exists with ID: {}", orderId);
            return findById(orderId).isPresent();
        } catch (Exception e) {
            logger.error("Error checking existence of order with ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to check if order exists: " + orderId, e);
        }
    }
}