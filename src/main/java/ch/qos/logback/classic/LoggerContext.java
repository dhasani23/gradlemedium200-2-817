package ch.qos.logback.classic;

import ch.qos.logback.core.Context;

/**
 * Stub implementation of LoggerContext
 */
public class LoggerContext implements Context {
    private String name;
    
    /**
     * Reset the context.
     */
    public void reset() {
        // Stub implementation
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getProperty(String key) {
        return null; // Stub implementation
    }
    
    @Override
    public void putProperty(String key, String value) {
        // Stub implementation
    }
}