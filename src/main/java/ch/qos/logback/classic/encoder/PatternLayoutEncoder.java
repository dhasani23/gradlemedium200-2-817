package ch.qos.logback.classic.encoder;

import ch.qos.logback.core.Context;

/**
 * Stub implementation of PatternLayoutEncoder
 */
public class PatternLayoutEncoder {
    private Context context;
    private String pattern;
    private boolean started;

    /**
     * Set the context.
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Get the context.
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Set the pattern.
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Get the pattern.
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Start the encoder.
     */
    public void start() {
        this.started = true;
    }

    /**
     * Check if the encoder is started.
     * @return true if the encoder is started, false otherwise
     */
    public boolean isStarted() {
        return started;
    }
}