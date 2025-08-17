package com.gradlemedium200.common.enums;

/**
 * Priority levels enumeration used for ordering and processing.
 * This enum provides priority levels that can be used throughout the application
 * to indicate the urgency or importance of various items.
 */
public enum PriorityEnum {
    
    /**
     * Low priority level - lowest urgency tasks or items
     */
    LOW(1),
    
    /**
     * Medium priority level - standard urgency tasks or items
     */
    MEDIUM(2),
    
    /**
     * High priority level - urgent tasks or items that need attention soon
     */
    HIGH(3),
    
    /**
     * Critical priority level - highest urgency tasks or items requiring immediate attention
     */
    CRITICAL(4);
    
    private final int value;
    
    /**
     * Constructor for PriorityEnum
     * 
     * @param value the numeric value representing the priority level
     */
    PriorityEnum(int value) {
        this.value = value;
    }
    
    /**
     * Get the numeric priority value
     * 
     * @return the numeric value of this priority level
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Get PriorityEnum from numeric value
     * 
     * @param value the numeric value to convert to a PriorityEnum
     * @return the corresponding PriorityEnum value or null if not found
     * @throws IllegalArgumentException if no matching enum value is found for the given numeric value
     */
    public static PriorityEnum fromValue(int value) {
        for (PriorityEnum priority : PriorityEnum.values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        
        // FIXME: Consider a more graceful handling of invalid priority values
        throw new IllegalArgumentException("Unknown priority value: " + value);
    }
    
    /**
     * Check if this priority is higher than the specified priority
     * 
     * @param other the priority to compare with
     * @return true if this priority is higher than the other, false otherwise
     */
    public boolean isHigherThan(PriorityEnum other) {
        return this.value > other.value;
    }
    
    /**
     * Check if this priority is lower than the specified priority
     * 
     * @param other the priority to compare with
     * @return true if this priority is lower than the other, false otherwise
     */
    public boolean isLowerThan(PriorityEnum other) {
        return this.value < other.value;
    }
    
    @Override
    public String toString() {
        // Return the name in a more readable format
        // TODO: Consider internationalization for priority names
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}