package ch.qos.logback.core;

/**
 * Stub implementation of ConsoleAppender
 * @param <E> The type of log events
 */
public class ConsoleAppender<E> extends UnsynchronizedAppenderBase<E> {
    private Object encoder;
    
    /**
     * Set the encoder.
     * @param encoder the encoder to set
     */
    public void setEncoder(Object encoder) {
        this.encoder = encoder;
    }
    
    /**
     * Get the encoder.
     * @return the encoder
     */
    public Object getEncoder() {
        return encoder;
    }
    
    @Override
    protected void append(E eventObject) {
        // Stub implementation
    }
}