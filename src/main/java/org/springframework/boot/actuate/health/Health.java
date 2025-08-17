package org.springframework.boot.actuate.health;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stub implementation of Health class from Spring Boot Actuator.
 */
public class Health {
    
    private final Status status;
    private final Map<String, Object> details;
    
    /**
     * Constructor.
     * @param builder the builder to build from
     */
    private Health(Builder builder) {
        this.status = builder.status;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(builder.details));
    }
    
    /**
     * Get the status.
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Get the details.
     * @return the details
     */
    public Map<String, Object> getDetails() {
        return details;
    }
    
    /**
     * Create a builder with UP status.
     * @return the builder
     */
    public static Builder up() {
        return status(Status.UP);
    }
    
    /**
     * Create a builder with DOWN status.
     * @return the builder
     */
    public static Builder down() {
        return status(Status.DOWN);
    }
    
    /**
     * Create a builder with OUT_OF_SERVICE status.
     * @return the builder
     */
    public static Builder outOfService() {
        return status(Status.OUT_OF_SERVICE);
    }
    
    /**
     * Create a builder with UNKNOWN status.
     * @return the builder
     */
    public static Builder unknown() {
        return status(Status.UNKNOWN);
    }
    
    /**
     * Create a builder with the given status.
     * @param status the status
     * @return the builder
     */
    public static Builder status(Status status) {
        return new Builder(status);
    }
    
    /**
     * Create a builder with the given status code.
     * @param statusCode the status code
     * @return the builder
     */
    public static Builder status(String statusCode) {
        return status(new Status(statusCode));
    }
    
    /**
     * Builder for Health.
     */
    public static class Builder {
        private final Status status;
        private final Map<String, Object> details;
        
        /**
         * Constructor.
         * @param status the status
         */
        public Builder(Status status) {
            this.status = status;
            this.details = new LinkedHashMap<>();
        }
        
        /**
         * Add a detail to the health.
         * @param key the detail key
         * @param value the detail value
         * @return this builder
         */
        public Builder withDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }
        
        /**
         * Add details to the health.
         * @param details the details to add
         * @return this builder
         */
        public Builder withDetails(Map<String, ?> details) {
            this.details.putAll(details);
            return this;
        }
        
        /**
         * Build the Health.
         * @return the Health
         */
        public Health build() {
            return new Health(this);
        }
    }
    
    /**
     * Status enum representing different health states.
     */
    public static class Status {
        /**
         * The UP status.
         */
        public static final Status UP = new Status("UP");
        
        /**
         * The DOWN status.
         */
        public static final Status DOWN = new Status("DOWN");
        
        /**
         * The OUT_OF_SERVICE status.
         */
        public static final Status OUT_OF_SERVICE = new Status("OUT_OF_SERVICE");
        
        /**
         * The UNKNOWN status.
         */
        public static final Status UNKNOWN = new Status("UNKNOWN");
        
        private final String code;
        
        /**
         * Constructor.
         * @param code the status code
         */
        public Status(String code) {
            this.code = code;
        }
        
        /**
         * Get the code.
         * @return the code
         */
        public String getCode() {
            return code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
}