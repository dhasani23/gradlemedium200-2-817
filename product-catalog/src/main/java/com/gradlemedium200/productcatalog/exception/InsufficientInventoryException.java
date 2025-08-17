package com.gradlemedium200.productcatalog.exception;

/**
 * Custom exception thrown when there is insufficient inventory for a product.
 * This exception is used when a requested quantity exceeds the available quantity
 * in inventory for a specific product.
 */
public class InsufficientInventoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * The ID of the product with insufficient inventory
     */
    private final String productId;
    
    /**
     * The quantity that was requested
     */
    private final int requestedQuantity;
    
    /**
     * The quantity that is currently available in inventory
     */
    private final int availableQuantity;
    
    /**
     * Constructor with inventory details.
     * 
     * @param productId The ID of the product with insufficient inventory
     * @param requestedQuantity The requested quantity that caused the exception
     * @param availableQuantity The available quantity in inventory
     */
    public InsufficientInventoryException(String productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient inventory for product '%s'. Requested: %d, Available: %d", 
                productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    /**
     * Constructor with custom message and inventory details.
     * 
     * @param message Custom error message
     * @param productId The ID of the product with insufficient inventory
     * @param requestedQuantity The requested quantity that caused the exception
     * @param availableQuantity The available quantity in inventory
     */
    public InsufficientInventoryException(String message, String productId, int requestedQuantity, int availableQuantity) {
        super(message);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    /**
     * Get the product ID with insufficient inventory.
     * 
     * @return The product ID
     */
    public String getProductId() {
        return productId;
    }
    
    /**
     * Get the requested quantity that caused the exception.
     * 
     * @return The requested quantity
     */
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    /**
     * Get the available quantity in inventory.
     * 
     * @return The available quantity
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }
    
    /**
     * Calculate the shortage amount (requested - available).
     * 
     * @return The shortage amount
     */
    public int getShortageAmount() {
        return requestedQuantity - availableQuantity;
    }
    
    // TODO: Add support for batch inventory checks with multiple product shortages
    
    // FIXME: Consider adding support for expected restock date information
}