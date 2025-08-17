package ch.qos.logback.core.spi;

/**
 * Interface for objects that have a life cycle.
 */
public interface LifeCycle {
    /**
     * Start the object.
     */
    void start();

    /**
     * Stop the object.
     */
    void stop();

    /**
     * Check if the object is started.
     * @return true if the object is started, false otherwise
     */
    boolean isStarted();
}