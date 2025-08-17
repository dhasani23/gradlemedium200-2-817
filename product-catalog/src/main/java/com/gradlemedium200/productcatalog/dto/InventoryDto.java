package com.gradlemedium200.productcatalog.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for inventory information used in API operations.
 * This class represents inventory data for products in warehouses and is used
 * for transferring inventory information between layers of the application.
 */
public class InventoryDto {

    private String productId;
    private String warehouseId;
    private int quantityAvailable;
    private int quantityReserved;
    private int totalQuantity;
    private int reorderLevel;
    private boolean isLowStock;
    private LocalDateTime lastUpdated;

    /**
     * Default constructor
     */
    public InventoryDto() {
        // Default constructor required for serialization/deserialization
    }

    /**
     * Fully-parameterized constructor for creating a complete inventory representation
     * 
     * @param productId The product identifier
     * @param warehouseId The warehouse identifier
     * @param quantityAvailable Available quantity in stock
     * @param quantityReserved Reserved quantity (in cart, pending orders)
     * @param totalQuantity Total quantity in warehouse
     * @param reorderLevel Reorder threshold level
     * @param isLowStock Flag indicating if inventory is below reorder level
     * @param lastUpdated Timestamp of last inventory update
     */
    public InventoryDto(String productId, String warehouseId, int quantityAvailable,
                        int quantityReserved, int totalQuantity, int reorderLevel,
                        boolean isLowStock, LocalDateTime lastUpdated) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = quantityReserved;
        this.totalQuantity = totalQuantity;
        this.reorderLevel = reorderLevel;
        this.isLowStock = isLowStock;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Get product ID
     * 
     * @return The product identifier
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Set product ID
     * 
     * @param productId The product identifier
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get warehouse ID
     * 
     * @return The warehouse identifier
     */
    public String getWarehouseId() {
        return warehouseId;
    }

    /**
     * Set warehouse ID
     * 
     * @param warehouseId The warehouse identifier
     */
    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    /**
     * Get available quantity
     * 
     * @return Available quantity in stock
     */
    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    /**
     * Set available quantity
     * 
     * @param quantityAvailable Available quantity in stock
     */
    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
        
        // Update low stock status when quantity is updated
        updateLowStockStatus();
    }

    /**
     * Get reserved quantity
     * 
     * @return Reserved quantity in pending orders or carts
     */
    public int getQuantityReserved() {
        return quantityReserved;
    }

    /**
     * Set reserved quantity
     * 
     * @param quantityReserved Reserved quantity in pending orders or carts
     */
    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    /**
     * Get total quantity
     * 
     * @return Total quantity in warehouse (available + reserved)
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Set total quantity
     * 
     * @param totalQuantity Total quantity in warehouse
     */
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    /**
     * Get reorder level
     * 
     * @return Reorder threshold level
     */
    public int getReorderLevel() {
        return reorderLevel;
    }

    /**
     * Set reorder level
     * 
     * @param reorderLevel Reorder threshold level
     */
    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
        
        // Update low stock status when reorder level is updated
        updateLowStockStatus();
    }

    /**
     * Check if inventory is low
     * 
     * @return True if inventory is below reorder level
     */
    public boolean isLowStock() {
        return isLowStock;
    }

    /**
     * Set low stock status
     * 
     * @param isLowStock Whether inventory is below reorder level
     */
    public void setLowStock(boolean isLowStock) {
        this.isLowStock = isLowStock;
    }

    /**
     * Get last updated timestamp
     * 
     * @return Time of last inventory update
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set last updated timestamp
     * 
     * @param lastUpdated Time of last inventory update
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Check if requested quantity can be fulfilled from available inventory
     * 
     * @param requestedQuantity The quantity requested for an order
     * @return True if the requested quantity can be fulfilled, false otherwise
     */
    public boolean canFulfillOrder(int requestedQuantity) {
        // Check if requested quantity is positive and less than or equal to available quantity
        if (requestedQuantity <= 0) {
            // FIXME: Consider throwing an IllegalArgumentException for negative quantities
            return false;
        }
        
        return requestedQuantity <= quantityAvailable;
    }
    
    /**
     * Updates the low stock status based on current quantity and reorder level
     */
    private void updateLowStockStatus() {
        this.isLowStock = (this.quantityAvailable <= this.reorderLevel);
        
        // TODO: Consider adding notification logic when stock becomes low
    }
    
    @Override
    public String toString() {
        return "InventoryDto{" +
                "productId='" + productId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", quantityAvailable=" + quantityAvailable +
                ", quantityReserved=" + quantityReserved +
                ", totalQuantity=" + totalQuantity +
                ", reorderLevel=" + reorderLevel +
                ", isLowStock=" + isLowStock +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}