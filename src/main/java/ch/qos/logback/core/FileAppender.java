package ch.qos.logback.core;

/**
 * Stub implementation of FileAppender
 * @param <E> The type of log events
 */
public class FileAppender<E> extends UnsynchronizedAppenderBase<E> {
    private String file;
    private boolean append;
    private boolean prudent;
    private Object bufferSize;
    private Object encoder;
    
    /**
     * Set the file path.
     * @param file the file path to set
     */
    public void setFile(String file) {
        this.file = file;
    }
    
    /**
     * Get the file path.
     * @return the file path
     */
    public String getFile() {
        return file;
    }
    
    /**
     * Set whether to append to an existing file.
     * @param append true to append, false to overwrite
     */
    public void setAppend(boolean append) {
        this.append = append;
    }
    
    /**
     * Get whether to append to an existing file.
     * @return true if appending, false if overwriting
     */
    public boolean isAppend() {
        return append;
    }
    
    /**
     * Set whether to use prudent mode.
     * @param prudent true to use prudent mode, false otherwise
     */
    public void setPrudent(boolean prudent) {
        this.prudent = prudent;
    }
    
    /**
     * Get whether to use prudent mode.
     * @return true if using prudent mode, false otherwise
     */
    public boolean isPrudent() {
        return prudent;
    }
    
    /**
     * Set the buffer size.
     * @param bufferSize the buffer size to set
     */
    public void setBufferSize(Object bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    /**
     * Get the buffer size.
     * @return the buffer size
     */
    public Object getBufferSize() {
        return bufferSize;
    }
    
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