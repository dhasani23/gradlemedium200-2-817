package ch.qos.logback.core.util;

/**
 * Stub implementation of FileSize class
 */
public class FileSize {
    private final long size;
    
    private FileSize(long size) {
        this.size = size;
    }
    
    /**
     * Get the size in bytes.
     * @return the size in bytes
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Create a FileSize from a string value.
     * @param value the value to parse
     * @return the FileSize
     */
    public static FileSize valueOf(String value) {
        // Stub implementation - just return a fixed size
        return new FileSize(8192);
    }
}