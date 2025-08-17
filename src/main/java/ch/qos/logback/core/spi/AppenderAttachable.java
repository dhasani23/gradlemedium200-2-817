package ch.qos.logback.core.spi;

import ch.qos.logback.core.Appender;
import java.util.Iterator;

/**
 * Interface for attaching appenders.
 * @param <E> The type of log events that can be appended
 */
public interface AppenderAttachable<E> {

    /**
     * Add an appender.
     * @param newAppender the appender to add
     */
    void addAppender(Appender<E> newAppender);

    /**
     * Get an iterator to iterate over all appenders.
     * @return iterator over attached appenders
     */
    Iterator<Appender<E>> iteratorForAppenders();

    /**
     * Get an appender by name.
     * @param name the name of the appender to get
     * @return the appender with the given name, or null if no such appender exists
     */
    Appender<E> getAppender(String name);

    /**
     * Check if an appender is attached.
     * @param appender the appender to check
     * @return true if the appender is attached, false otherwise
     */
    boolean isAttached(Appender<E> appender);

    /**
     * Detach and stop all appenders.
     */
    void detachAndStopAllAppenders();

    /**
     * Detach an appender.
     * @param appender the appender to detach
     * @return true if the appender was detached, false otherwise
     */
    boolean detachAppender(Appender<E> appender);

    /**
     * Detach an appender by name.
     * @param name the name of the appender to detach
     * @return true if the appender was detached, false otherwise
     */
    boolean detachAppender(String name);
}