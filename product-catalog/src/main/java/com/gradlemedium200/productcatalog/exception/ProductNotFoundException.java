package com.gradlemedium200.productcatalog.exception;

/**
 * Custom exception thrown when a product is not found in the catalog.
 * This exception is used to indicate that a requested product does not exist
 * in the product catalog database.
 */
public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * The ID of the product that was not found
     */
    private final String productId;
    
    /**
     * Constructor with product ID.
     * 
     * @param productId The ID of the product that was not found
     */
    public ProductNotFoundException(String productId) {
        super(String.format("Product with ID '%s' was not found", productId));
        this.productId = productId;
    }
    
    /**
     * Constructor with custom message.
     * 
     * @param message Custom error message
     */
    public ProductNotFoundException(String message, String productId) {
        super(message);
        this.productId = productId;
    }
    
    /**
     * Get the product ID that was not found.
     * 
     * @return The product ID
     */
    public String getProductId() {
        return productId;
    }
    
    // FIXME: Add additional constructors to handle cases where product is looked up by other attributes
    
    // TODO: Consider adding exception cause parameter for better debugging
}