package com.gradlemedium200.orderservice.model;

/**
 * Enumeration defining all possible order states throughout the order lifecycle.
 */
public enum OrderStatus {
    /**
     * Order is created but not yet processed
     */
    PENDING,
    
    /**
     * Order has been confirmed and payment processed
     */
    CONFIRMED,
    
    /**
     * Order is being processed
     */
    PROCESSING,
    
    /**
     * Order has been shipped
     */
    SHIPPED,
    
    /**
     * Order has been delivered
     */
    DELIVERED,
    
    /**
     * Order has been cancelled
     */
    CANCELLED,
    
    /**
     * Order has been refunded
     */
    REFUNDED;
    
    /**
     * Checks if the current status can transition to the target status.
     * Implements basic order flow validation rules.
     *
     * @param targetStatus The target status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        // No transitions allowed from final states
        if (this == CANCELLED || this == REFUNDED || this == DELIVERED) {
            return false;
        }
        
        // Define allowed transitions
        switch(this) {
            case PENDING:
                return targetStatus == CONFIRMED || targetStatus == CANCELLED;
                
            case CONFIRMED:
                return targetStatus == PROCESSING || targetStatus == CANCELLED || targetStatus == REFUNDED;
                
            case PROCESSING:
                return targetStatus == SHIPPED || targetStatus == CANCELLED || targetStatus == REFUNDED;
                
            case SHIPPED:
                return targetStatus == DELIVERED;
                
            default:
                return false;
        }
    }
    
    /**
     * Returns the name of the enum constant as a string.
     * This method is added for compatibility with code that expects a String method.
     *
     * @return The name of the enum constant
     */
    public String getStatusName() {
        return name();
    }
}