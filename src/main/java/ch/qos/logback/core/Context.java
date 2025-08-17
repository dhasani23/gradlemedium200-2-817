package ch.qos.logback.core;

/**
 * Interface for the context in which logback operates.
 */
public interface Context {
    /**
     * Get the name of the context.
     * @return the context name
     */
    String getName();

    /**
     * Set the name of the context.
     * @param name the name to set
     */
    void setName(String name);

    /**
     * Get a property from the context.
     * @param key the property key
     * @return the property value, or null if the key is not found
     */
    String getProperty(String key);

    /**
     * Set a property in the context.
     * @param key the property key
     * @param value the property value
     */
    void putProperty(String key, String value);
}