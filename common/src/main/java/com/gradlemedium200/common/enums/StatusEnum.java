package com.gradlemedium200.common.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Common status enumeration for various entities across modules.
 * This enum represents different states that an entity can have within the system.
 */
public enum StatusEnum {
    /**
     * Active status - Entity is fully active and operational
     */
    ACTIVE("active"),
    
    /**
     * Inactive status - Entity exists but is not currently operational
     */
    INACTIVE("inactive"),
    
    /**
     * Pending status - Entity is waiting for approval or activation
     */
    PENDING("pending"),
    
    /**
     * Deleted status - Entity has been marked for deletion
     */
    DELETED("deleted"),
    
    /**
     * Suspended status - Entity temporarily unavailable due to policy violations or other issues
     */
    SUSPENDED("suspended");
    
    private final String value;
    
    // Cache for faster lookup from string value to enum
    private static final Map<String, StatusEnum> CACHE = new HashMap<>();
    
    static {
        // Initialize the lookup cache for faster fromValue operation
        for (StatusEnum status : StatusEnum.values()) {
            CACHE.put(status.getValue(), status);
        }
    }
    
    /**
     * Constructor for status enum
     * 
     * @param value String representation of the status
     */
    StatusEnum(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of this status.
     *
     * @return The string representation of this status
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Converts a string value to the corresponding StatusEnum.
     *
     * @param value The string representation to convert
     * @return The matching StatusEnum, or null if no match is found
     * @throws IllegalArgumentException if the value doesn't match any enum
     */
    public static StatusEnum fromValue(String value) {
        // Check cache first for performance
        StatusEnum status = CACHE.get(value);
        
        if (status != null) {
            return status;
        }
        
        // Fall back to linear search if cache miss (should not happen with properly initialized cache)
        return Arrays.stream(StatusEnum.values())
                .filter(s -> s.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown status value: " + value + ". Valid values are: " + 
                        Arrays.toString(StatusEnum.values())));
    }
    
    /**
     * Check if the status represents an active state
     * 
     * @return true if this status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Check if the status represents any kind of inactive state
     * 
     * @return true if this status is not ACTIVE, false otherwise
     */
    public boolean isInactive() {
        return this != ACTIVE;
    }
    
    /**
     * Check if the entity is in a terminal state (deleted)
     * 
     * @return true if this status is DELETED, false otherwise
     */
    public boolean isTerminal() {
        return this == DELETED;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}