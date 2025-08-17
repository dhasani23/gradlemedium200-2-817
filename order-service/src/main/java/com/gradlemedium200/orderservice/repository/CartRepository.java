package com.gradlemedium200.orderservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.orderservice.model.ShoppingCart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data access layer for shopping cart persistence operations.
 * Provides CRUD operations for ShoppingCart entities using DynamoDB.
 */
@Repository
public class CartRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(CartRepository.class);
    
    private final DynamoDBMapper dynamoDbTemplate;
    
    /**
     * Constructs a CartRepository with the required DynamoDBMapper.
     * 
     * @param dynamoDbTemplate DynamoDB mapper for data operations
     */
    @Autowired
    public CartRepository(DynamoDBMapper dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }
    
    /**
     * Saves a shopping cart to DynamoDB.
     * 
     * @param cart The shopping cart to save
     * @return The saved shopping cart
     */
    public ShoppingCart save(ShoppingCart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Shopping cart cannot be null");
        }
        
        try {
            logger.debug("Saving shopping cart with ID: {}", cart.getCartId());
            dynamoDbTemplate.save(cart);
            return cart;
        } catch (Exception e) {
            logger.error("Failed to save shopping cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save shopping cart", e);
        }
    }
    
    /**
     * Finds a shopping cart by customer ID.
     * 
     * @param customerId The customer ID
     * @return Optional containing the shopping cart if found, empty otherwise
     */
    public Optional<ShoppingCart> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        try {
            logger.debug("Finding shopping cart for customer ID: {}", customerId);
            
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":customerId", new AttributeValue().withS(customerId));
            
            DynamoDBQueryExpression<ShoppingCart> queryExpression = new DynamoDBQueryExpression<ShoppingCart>()
                    .withIndexName("CustomerIdIndex")
                    .withConsistentRead(false)
                    .withKeyConditionExpression("customerId = :customerId")
                    .withExpressionAttributeValues(eav);
            
            List<ShoppingCart> result = dynamoDbTemplate.query(ShoppingCart.class, queryExpression);
            
            if (result != null && !result.isEmpty()) {
                return Optional.of(result.get(0));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find shopping cart for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to find shopping cart for customer ID: " + customerId, e);
        }
    }
    
    /**
     * Deletes a shopping cart by ID.
     * 
     * @param cartId The cart ID to delete
     */
    public void delete(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cart ID cannot be null or empty");
        }
        
        try {
            logger.debug("Deleting shopping cart with ID: {}", cartId);
            
            // Create a ShoppingCart with only the hashkey (cartId) set
            ShoppingCart cartKey = new ShoppingCart();
            cartKey.setCartId(cartId);
            
            dynamoDbTemplate.delete(cartKey);
        } catch (Exception e) {
            logger.error("Failed to delete shopping cart with ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete shopping cart with ID: " + cartId, e);
        }
    }
    
    /**
     * Checks if a cart exists for the given customer ID.
     * 
     * @param customerId The customer ID to check
     * @return true if a cart exists for the customer, false otherwise
     */
    public boolean existsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        try {
            logger.debug("Checking if shopping cart exists for customer ID: {}", customerId);
            return findByCustomerId(customerId).isPresent();
        } catch (Exception e) {
            logger.error("Failed to check if shopping cart exists for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to check if shopping cart exists for customer ID: " + customerId, e);
        }
    }
    
    /**
     * Finds a shopping cart by its ID.
     * 
     * @param cartId The cart ID
     * @return Optional containing the shopping cart if found, empty otherwise
     */
    public Optional<ShoppingCart> findById(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cart ID cannot be null or empty");
        }
        
        try {
            logger.debug("Finding shopping cart with ID: {}", cartId);
            ShoppingCart cart = dynamoDbTemplate.load(ShoppingCart.class, cartId);
            return Optional.ofNullable(cart);
        } catch (Exception e) {
            logger.error("Failed to find shopping cart with ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to find shopping cart with ID: " + cartId, e);
        }
    }
    
    /**
     * Deletes a shopping cart by customer ID.
     * 
     * @param customerId The customer ID whose cart to delete
     * @return true if a cart was found and deleted, false otherwise
     */
    public boolean deleteByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        try {
            logger.debug("Deleting shopping cart for customer ID: {}", customerId);
            Optional<ShoppingCart> cartOptional = findByCustomerId(customerId);
            
            if (cartOptional.isPresent()) {
                dynamoDbTemplate.delete(cartOptional.get());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Failed to delete shopping cart for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete shopping cart for customer ID: " + customerId, e);
        }
    }
    
    // TODO: Add batch operations for processing multiple carts at once
    
    // FIXME: Secondary index for customer ID needs to be created in DynamoDB table
}