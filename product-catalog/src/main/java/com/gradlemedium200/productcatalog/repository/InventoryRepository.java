package com.gradlemedium200.productcatalog.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.productcatalog.model.Inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for inventory data access operations using DynamoDB.
 * Provides methods for saving, retrieving, and updating inventory records.
 */
@Repository
public class InventoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(InventoryRepository.class);
    
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Constructor with dependency injection for DynamoDB mapper.
     *
     * @param dynamoDBMapper DynamoDB mapper for inventory operations
     */
    @Autowired
    public InventoryRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Save or update an inventory record.
     *
     * @param inventory The inventory record to save or update
     * @return The saved inventory record
     */
    public Inventory save(Inventory inventory) {
        // If it's a new inventory record, generate a unique ID
        if (inventory.getInventoryId() == null || inventory.getInventoryId().isEmpty()) {
            inventory.setInventoryId(UUID.randomUUID().toString());
            inventory.setUpdatedAt(LocalDateTime.now());
        }
        
        logger.debug("Saving inventory record: {}", inventory);
        dynamoDBMapper.save(inventory);
        return inventory;
    }

    /**
     * Find inventory by product ID.
     *
     * @param productId The product ID to search for
     * @return Optional containing the inventory if found, empty otherwise
     */
    public Optional<Inventory> findByProductId(String productId) {
        if (productId == null || productId.isEmpty()) {
            logger.warn("Attempted to find inventory with null or empty product ID");
            return Optional.empty();
        }
        
        logger.debug("Finding inventory for product ID: {}", productId);
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":productId", new AttributeValue().withS(productId));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("ProductId = :productId")
                .withExpressionAttributeValues(eav);
        
        List<Inventory> result = dynamoDBMapper.scan(Inventory.class, scanExpression);
        
        if (result != null && !result.isEmpty()) {
            return Optional.of(result.get(0));
        }
        
        logger.debug("No inventory found for product ID: {}", productId);
        return Optional.empty();
    }

    /**
     * Find all inventory records for a specific warehouse.
     *
     * @param warehouseId The warehouse ID to search for
     * @return List of inventory records for the warehouse
     */
    public List<Inventory> findByWarehouseId(String warehouseId) {
        if (warehouseId == null || warehouseId.isEmpty()) {
            logger.warn("Attempted to find inventory with null or empty warehouse ID");
            throw new IllegalArgumentException("Warehouse ID cannot be null or empty");
        }
        
        logger.debug("Finding inventory for warehouse ID: {}", warehouseId);
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":warehouseId", new AttributeValue().withS(warehouseId));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("WarehouseId = :warehouseId")
                .withExpressionAttributeValues(eav);
        
        return dynamoDBMapper.scan(Inventory.class, scanExpression);
    }

    /**
     * Find items with low stock levels (where available quantity is at or below reorder level).
     *
     * @return List of inventory records with low stock
     */
    public List<Inventory> findLowStockItems() {
        logger.debug("Finding low stock inventory items");
        
        // DynamoDB doesn't support direct comparisons between attributes in filter expressions,
        // so we need to scan all records and filter in-memory
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        
        List<Inventory> allInventory = dynamoDBMapper.scan(Inventory.class, scanExpression);
        
        // Filter in memory for low stock items
        // TODO: Consider implementing a GSI with a boolean LowStock attribute that is updated on save
        return allInventory.stream()
                .filter(Inventory::isLowStock)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Update inventory quantity for a product.
     *
     * @param productId       The product ID to update
     * @param quantityChange  The quantity change (positive for increase, negative for decrease)
     * @return true if update successful, false otherwise
     */
    public boolean updateQuantity(String productId, int quantityChange) {
        if (productId == null || productId.isEmpty()) {
            logger.warn("Attempted to update inventory with null or empty product ID");
            return false;
        }
        
        logger.debug("Updating quantity by {} for product ID: {}", quantityChange, productId);
        
        Optional<Inventory> inventoryOpt = findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Cannot update quantity - inventory not found for product ID: {}", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        int currentQuantity = inventory.getQuantityAvailable();
        int newQuantity = currentQuantity + quantityChange;
        
        // Check if the update would result in negative stock
        if (newQuantity < 0) {
            logger.warn("Cannot update quantity - would result in negative stock for product ID: {}", productId);
            return false;
        }
        
        // Update the inventory quantity
        inventory.setQuantityAvailable(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        // If we're adding stock, treat it as a restock operation
        if (quantityChange > 0) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }
        
        // Save the updated inventory
        dynamoDBMapper.save(inventory);
        logger.debug("Successfully updated quantity for product ID: {}. New quantity: {}", productId, newQuantity);
        
        return true;
    }
    
    /**
     * Delete an inventory record.
     *
     * @param inventory The inventory record to delete
     */
    public void delete(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        
        logger.debug("Deleting inventory record: {}", inventory);
        dynamoDBMapper.delete(inventory);
    }
    
    /**
     * Get inventory by ID.
     *
     * @param inventoryId The inventory ID
     * @return Optional containing the inventory if found, empty otherwise
     */
    public Optional<Inventory> findById(String inventoryId) {
        if (inventoryId == null || inventoryId.isEmpty()) {
            return Optional.empty();
        }
        
        Inventory inventory = dynamoDBMapper.load(Inventory.class, inventoryId);
        return Optional.ofNullable(inventory);
    }
}