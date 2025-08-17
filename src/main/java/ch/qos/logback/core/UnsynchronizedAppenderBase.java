package ch.qos.logback.core;

/**
 * Abstract base class for appenders.
 * @param <E> The type of log events that can be appended
 */
public abstract class UnsynchronizedAppenderBase<E> implements Appender<E> {
    protected boolean started = false;
    protected String name;
    protected Context context;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean isStarted() {
        return started;
    }
    
    @Override
    public void start() {
        started = true;
    }
    
    @Override
    public void stop() {
        started = false;
    }
    
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    
    @Override
    public Context getContext() {
        return context;
    }
    
    @Override
    public void doAppend(E eventObject) {
        if (!isStarted()) {
            return;
        }
        
        append(eventObject);
    }
    
    protected abstract void append(E eventObject);
    
    protected void addError(String msg) {
        System.err.println("[ERROR] " + msg);
    }

    @Override
    public void addFilter(Object filter) {
        // Stub implementation
    }

    @Override
    public void clearAllFilters() {
        // Stub implementation
    }

    @Override
    public Object getFirstFilter() {
        return null; // Stub implementation
    }

    @Override
    public void getCopyOfAttachedFiltersList() {
        // Stub implementation
    }
}