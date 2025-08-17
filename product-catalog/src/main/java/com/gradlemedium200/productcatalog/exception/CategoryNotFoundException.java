package com.gradlemedium200.productcatalog.exception;

/**
 * Custom exception thrown when a category is not found in the product catalog.
 * This exception provides additional context about which category was being
 * searched for when the exception occurred.
 *
 * @author gradlemedium200
 */
public class CategoryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * The ID of the category that was not found
     */
    private final String categoryId;
    
    /**
     * Constructs a new CategoryNotFoundException with the specified category ID.
     * The exception message is automatically generated based on the category ID.
     *
     * @param categoryId the ID of the category that was not found
     */
    public CategoryNotFoundException(String categoryId) {
        super("Category not found with ID: " + categoryId);
        this.categoryId = categoryId;
        
        // TODO: Consider adding logging of this exception for monitoring purposes
    }
    
    /**
     * Constructs a new CategoryNotFoundException with a custom message and category ID.
     *
     * @param message the custom error message
     * @param categoryId the ID of the category that was not found
     */
    public CategoryNotFoundException(String message, String categoryId) {
        super(message);
        this.categoryId = categoryId;
    }
    
    /**
     * Returns the ID of the category that was not found.
     *
     * @return the category ID that was not found, or null if not specified
     */
    public String getCategoryId() {
        return categoryId;
    }
}