package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.gradlemedium200.productcatalog.util.LocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * Entity model representing inventory information for products with stock tracking.
 * Stores inventory data in DynamoDB for high performance stock management.
 */
@DynamoDBTable(tableName = "Inventory")
public class Inventory {

    private String inventoryId;
    private String productId;
    private String warehouseId;
    private int quantityAvailable;
    private int quantityReserved;
    private int reorderLevel;
    private int maxStockLevel;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by DynamoDB mapper.
     */
    public Inventory() {
    }

    /**
     * Constructor with required fields for inventory tracking.
     * 
     * @param inventoryId       Unique identifier for inventory record
     * @param productId         ID of the product this inventory tracks
     * @param warehouseId       ID of the warehouse where inventory is stored
     * @param quantityAvailable Available quantity in stock
     * @param reorderLevel      Minimum stock level that triggers reorder
     * @param maxStockLevel     Maximum stock level
     */
    public Inventory(String inventoryId, String productId, String warehouseId, 
                     int quantityAvailable, int reorderLevel, int maxStockLevel) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = 0;
        this.reorderLevel = reorderLevel;
        this.maxStockLevel = maxStockLevel;
        this.lastRestockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get inventory ID.
     *
     * @return The unique identifier for this inventory record
     */
    @DynamoDBHashKey(attributeName = "InventoryId")
    public String getInventoryId() {
        return inventoryId;
    }

    /**
     * Set inventory ID.
     *
     * @param inventoryId The unique identifier for this inventory record
     */
    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    /**
     * Get the product ID associated with this inventory.
     *
     * @return The product ID
     */
    @DynamoDBAttribute(attributeName = "ProductId")
    public String getProductId() {
        return productId;
    }

    /**
     * Set the product ID associated with this inventory.
     *
     * @param productId The product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get the warehouse ID where this inventory is stored.
     *
     * @return The warehouse ID
     */
    @DynamoDBAttribute(attributeName = "WarehouseId")
    public String getWarehouseId() {
        return warehouseId;
    }

    /**
     * Set the warehouse ID where this inventory is stored.
     *
     * @param warehouseId The warehouse ID
     */
    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    /**
     * Get available quantity in stock.
     *
     * @return The available quantity
     */
    @DynamoDBAttribute(attributeName = "QuantityAvailable")
    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    /**
     * Set available quantity in stock.
     *
     * @param quantityAvailable The available quantity
     */
    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get reserved quantity for pending orders.
     *
     * @return The reserved quantity
     */
    @DynamoDBAttribute(attributeName = "QuantityReserved")
    public int getQuantityReserved() {
        return quantityReserved;
    }

    /**
     * Set reserved quantity for pending orders.
     *
     * @param quantityReserved The reserved quantity
     */
    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get minimum stock level that triggers reorder.
     *
     * @return The reorder level
     */
    @DynamoDBAttribute(attributeName = "ReorderLevel")
    public int getReorderLevel() {
        return reorderLevel;
    }

    /**
     * Set minimum stock level that triggers reorder.
     *
     * @param reorderLevel The reorder level
     */
    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    /**
     * Get maximum stock level.
     *
     * @return The maximum stock level
     */
    @DynamoDBAttribute(attributeName = "MaxStockLevel")
    public int getMaxStockLevel() {
        return maxStockLevel;
    }

    /**
     * Set maximum stock level.
     *
     * @param maxStockLevel The maximum stock level
     */
    public void setMaxStockLevel(int maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    /**
     * Get last restock timestamp.
     *
     * @return The last restocked timestamp
     */
    @DynamoDBAttribute(attributeName = "LastRestockedAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getLastRestockedAt() {
        return lastRestockedAt;
    }

    /**
     * Set last restock timestamp.
     *
     * @param lastRestockedAt The last restocked timestamp
     */
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) {
        this.lastRestockedAt = lastRestockedAt;
    }

    /**
     * Get last inventory update timestamp.
     *
     * @return The last updated timestamp
     */
    @DynamoDBAttribute(attributeName = "UpdatedAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set last inventory update timestamp.
     *
     * @param updatedAt The last updated timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get total available quantity (available - reserved).
     *
     * @return The total available quantity
     */
    public int getTotalAvailable() {
        return Math.max(0, quantityAvailable - quantityReserved);
    }

    /**
     * Check if inventory is below reorder level.
     *
     * @return True if the available quantity is below reorder level, false otherwise
     */
    public boolean isLowStock() {
        return quantityAvailable <= reorderLevel;
    }

    /**
     * Reserve specified quantity if available.
     *
     * @param quantity The quantity to reserve
     * @return True if reservation was successful, false if insufficient stock
     */
    public boolean reserveQuantity(int quantity) {
        if (quantity <= 0) {
            // Cannot reserve negative or zero quantity
            return false;
        }

        if (getTotalAvailable() >= quantity) {
            quantityReserved += quantity;
            updatedAt = LocalDateTime.now();
            return true;
        }
        
        // Insufficient stock available for reservation
        return false;
    }

    /**
     * Release reserved quantity back to available stock.
     * 
     * @param quantity The quantity to release from reservation
     */
    public void releaseReserved(int quantity) {
        if (quantity <= 0) {
            return;
        }
        
        // Don't release more than what is actually reserved
        int actualRelease = Math.min(quantity, quantityReserved);
        quantityReserved -= actualRelease;
        updatedAt = LocalDateTime.now();
    }

    /**
     * Restock inventory with new items.
     * 
     * @param quantity The quantity to add to available stock
     * @return True if restock was successful, false if would exceed max stock level
     */
    public boolean restock(int quantity) {
        if (quantity <= 0) {
            return false;
        }
        
        if (quantityAvailable + quantity <= maxStockLevel) {
            quantityAvailable += quantity;
            lastRestockedAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
            return true;
        }
        
        // Would exceed max stock level
        return false;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId='" + inventoryId + '\'' +
                ", productId='" + productId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", quantityAvailable=" + quantityAvailable +
                ", quantityReserved=" + quantityReserved +
                ", reorderLevel=" + reorderLevel +
                ", maxStockLevel=" + maxStockLevel +
                ", totalAvailable=" + getTotalAvailable() +
                ", lastRestockedAt=" + lastRestockedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * Converter class for marshalling LocalDateTime objects to/from DynamoDB.
     * @deprecated Use {@link com.gradlemedium200.productcatalog.util.LocalDateTimeConverter} instead
     */
    @Deprecated
    public static class LocalDateTimeConverter extends com.gradlemedium200.productcatalog.util.LocalDateTimeConverter {
        // This class extends the common converter for backward compatibility
    }
}