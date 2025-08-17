package com.gradlemedium200.common.exception;

import com.gradlemedium200.common.constants.ErrorCodes;

/**
 * Exception thrown when a requested resource is not found.
 * This exception provides details about the missing resource including its type and ID.
 * 
 * @since 1.0
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Type of resource that was not found (e.g., "User", "Product", "Order")
     */
    private final String resourceType;
    
    /**
     * ID of the resource that was not found
     */
    private final String resourceId;
    
    /**
     * Constructor with resource type and ID.
     * Creates a ResourceNotFoundException with a standardized error message.
     * 
     * @param resourceType Type of resource that was not found
     * @param resourceId ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, 
              String.format("%s with id '%s' not found", resourceType, resourceId));
        
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        
        // Add context information
        addContext("resourceType", resourceType);
        addContext("resourceId", resourceId);
    }
    
    /**
     * Constructor with custom message.
     * Creates a ResourceNotFoundException with a custom error message.
     * 
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, message);
        
        // For custom messages, these fields are not applicable
        this.resourceType = null;
        this.resourceId = null;
        
        // TODO: Consider requiring resourceType even with custom messages for consistency
    }
    
    /**
     * Get the resource type.
     * 
     * @return The type of resource that was not found
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Get the resource ID.
     * 
     * @return The ID of the resource that was not found
     */
    public String getResourceId() {
        return resourceId;
    }
    
    /**
     * Provides a more detailed string representation including resource details.
     * 
     * @return A string representation of this exception with resource details
     */
    @Override
    public String toString() {
        if (resourceType != null && resourceId != null) {
            return String.format("%s: Resource '%s' with ID '%s' was not found", 
                                getClass().getSimpleName(), resourceType, resourceId);
        }
        
        return super.toString();
    }
    
    // FIXME: Consider adding support for composite keys or multiple identifiers
}