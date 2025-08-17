package ch.qos.logback.core.async;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.Iterator;

/**
 * Stub AsyncAppender class to avoid compilation issues.
 * This is a simplified version that mimics the interface of the actual AsyncAppender class.
 */
public class AsyncAppender<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    private AppenderAttachableImpl<E> appenderAttachable = new AppenderAttachableImpl<E>();
    private int queueSize = 256;
    private int discardingThreshold = 0;
    private boolean neverBlock = false;

    /**
     * Sets the maximum size of the backing queue.
     * 
     * @param queueSize the maximum size of the backing queue
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Gets the maximum size of the backing queue.
     * 
     * @return the maximum size of the backing queue
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Sets the threshold of queue remaining capacity below which discarding starts.
     * 
     * @param discardingThreshold the threshold to set
     */
    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    /**
     * Gets the discarding threshold.
     * 
     * @return the discarding threshold
     */
    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    /**
     * Sets whether the appender should never block when the queue is full.
     * 
     * @param neverBlock true if the appender should never block, false otherwise
     */
    public void setNeverBlock(boolean neverBlock) {
        this.neverBlock = neverBlock;
    }

    /**
     * Gets whether the appender is set to never block.
     * 
     * @return true if the appender is set to never block, false otherwise
     */
    public boolean isNeverBlock() {
        return neverBlock;
    }

    @Override
    protected void append(E eventObject) {
        // Stub implementation - in the real AsyncAppender, this would add the event to a queue
        // to be processed by a separate worker thread
        appenderAttachable.appendLoopOnAppenders(eventObject);
    }

    @Override
    public void start() {
        if (appenderAttachable.getAppenderCount() == 0) {
            addError("No appenders present in AsyncAppender");
            return;
        }
        super.start();
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        super.stop();
    }

    // AppenderAttachable methods

    @Override
    public void addAppender(Appender<E> newAppender) {
        appenderAttachable.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return appenderAttachable.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return appenderAttachable.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> appender) {
        return appenderAttachable.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        appenderAttachable.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> appender) {
        return appenderAttachable.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return appenderAttachable.detachAppender(name);
    }
}