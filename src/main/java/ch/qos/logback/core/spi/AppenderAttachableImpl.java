package ch.qos.logback.core.spi;

import ch.qos.logback.core.Appender;
import java.util.*;

/**
 * Stub implementation of the AppenderAttachable interface.
 * @param <E> The type of log events that can be appended
 */
public class AppenderAttachableImpl<E> implements AppenderAttachable<E> {

    private final Map<String, Appender<E>> appenderMap = new HashMap<>();

    /**
     * Add an appender.
     * @param newAppender the appender to add
     */
    @Override
    public void addAppender(Appender<E> newAppender) {
        if (newAppender == null || newAppender.getName() == null) {
            return;
        }
        appenderMap.put(newAppender.getName(), newAppender);
    }

    /**
     * Get an iterator to iterate over all appenders.
     * @return iterator over attached appenders
     */
    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return appenderMap.values().iterator();
    }

    /**
     * Get an appender by name.
     * @param name the name of the appender to get
     * @return the appender with the given name, or null if no such appender exists
     */
    @Override
    public Appender<E> getAppender(String name) {
        return appenderMap.get(name);
    }

    /**
     * Check if an appender is attached.
     * @param appender the appender to check
     * @return true if the appender is attached, false otherwise
     */
    @Override
    public boolean isAttached(Appender<E> appender) {
        if (appender == null || appender.getName() == null) {
            return false;
        }
        return appenderMap.containsKey(appender.getName());
    }

    /**
     * Detach and stop all appenders.
     */
    @Override
    public void detachAndStopAllAppenders() {
        for (Appender<E> appender : appenderMap.values()) {
            appender.stop();
        }
        appenderMap.clear();
    }

    /**
     * Detach an appender.
     * @param appender the appender to detach
     * @return true if the appender was detached, false otherwise
     */
    @Override
    public boolean detachAppender(Appender<E> appender) {
        if (appender == null || appender.getName() == null) {
            return false;
        }
        return appenderMap.remove(appender.getName()) != null;
    }

    /**
     * Detach an appender by name.
     * @param name the name of the appender to detach
     * @return true if the appender was detached, false otherwise
     */
    @Override
    public boolean detachAppender(String name) {
        if (name == null) {
            return false;
        }
        return appenderMap.remove(name) != null;
    }

    /**
     * Get the number of attached appenders.
     * @return the number of appenders
     */
    public int getAppenderCount() {
        return appenderMap.size();
    }

    /**
     * Call append on all attached appenders.
     * @param e the event to append
     */
    public void appendLoopOnAppenders(E e) {
        for (Appender<E> appender : appenderMap.values()) {
            if (appender.isStarted()) {
                appender.doAppend(e);
            }
        }
    }
}