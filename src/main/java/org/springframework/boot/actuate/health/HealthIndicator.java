package org.springframework.boot.actuate.health;

/**
 * Stub implementation of HealthIndicator interface from Spring Boot Actuator.
 */
public interface HealthIndicator {
    
    /**
     * Return the health of a component.
     * @return the health
     */
    Health health();
}