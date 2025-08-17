package ch.qos.logback.core;

import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.FilterAttachable;

/**
 * Appender interface for logging events.
 * @param <E> The type of log events that can be appended
 */
public interface Appender<E> extends LifeCycle, FilterAttachable<E> {

    /**
     * Get the name of the appender.
     * @return the name of the appender
     */
    String getName();

    /**
     * Set the name of the appender.
     * @param name the name of the appender
     */
    void setName(String name);

    /**
     * Do append method for adding events.
     * @param event the event to append
     */
    void doAppend(E event);

    /**
     * Set the context of the appender.
     * @param context the context to set
     */
    void setContext(Context context);

    /**
     * Get the context of the appender.
     * @return the context
     */
    Context getContext();
}