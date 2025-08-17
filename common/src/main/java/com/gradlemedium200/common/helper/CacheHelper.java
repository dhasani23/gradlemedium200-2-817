package com.gradlemedium200.common.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for caching operations and cache management.
 * Provides common cache operations like put, get, evict, clear, and compute-if-absent pattern.
 */
@Component
public class CacheHelper {
    
    private static final Logger logger = Logger.getLogger(CacheHelper.class.getName());
    
    private final CacheManager cacheManager;
    private final long defaultTtl;

    /**
     * Constructor with autowired dependencies.
     *
     * @param cacheManager Spring CacheManager instance
     * @param defaultTtl Default time-to-live for cache entries (in seconds)
     */
    @Autowired
    public CacheHelper(CacheManager cacheManager, 
                       @Value("${cache.default.ttl:300}") long defaultTtl) {
        this.cacheManager = cacheManager;
        this.defaultTtl = defaultTtl;
        logger.info("CacheHelper initialized with default TTL: " + defaultTtl + " seconds");
    }

    /**
     * Put value into cache with specified key.
     *
     * @param cacheName Name of the cache
     * @param key Cache key
     * @param value Value to store in cache
     */
    public void put(String cacheName, String key, Object value) {
        try {
            Cache cache = getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.fine("Added entry to cache '" + cacheName + "' with key: " + key);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to put value in cache '" + cacheName + "' for key: " + key, e);
        }
    }

    /**
     * Get value from cache by key.
     *
     * @param cacheName Name of the cache
     * @param key Cache key
     * @return Cached value or null if not found
     */
    public Object get(String cacheName, String key) {
        try {
            Cache cache = getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    logger.fine("Cache hit for '" + cacheName + "' with key: " + key);
                    return wrapper.get();
                } else {
                    logger.fine("Cache miss for '" + cacheName + "' with key: " + key);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get value from cache '" + cacheName + "' for key: " + key, e);
        }
        return null;
    }

    /**
     * Evict specific entry from cache.
     *
     * @param cacheName Name of the cache
     * @param key Cache key to evict
     */
    public void evict(String cacheName, String key) {
        try {
            Cache cache = getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                logger.fine("Evicted entry from cache '" + cacheName + "' with key: " + key);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to evict entry from cache '" + cacheName + "' for key: " + key, e);
        }
    }

    /**
     * Clear all entries from specified cache.
     *
     * @param cacheName Name of the cache to clear
     */
    public void clear(String cacheName) {
        try {
            Cache cache = getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Cleared all entries from cache: " + cacheName);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to clear cache: " + cacheName, e);
        }
    }

    /**
     * Get value from cache or compute and cache if not present.
     *
     * @param <T> Type of the cached value
     * @param cacheName Name of the cache
     * @param key Cache key
     * @param supplier Supplier function to compute value if not in cache
     * @return Cached value or computed value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String cacheName, String key, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "Supplier cannot be null");
        
        // Try to get from cache first
        Object value = get(cacheName, key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                logger.warning("Type mismatch for cached value with key: " + key + 
                               ". Expected: T, Found: " + value.getClass().getName());
                // Fall through to compute new value
            }
        }
        
        // Compute new value and store in cache
        T newValue = supplier.get();
        if (newValue != null) {
            put(cacheName, key, newValue);
        }
        
        return newValue;
    }
    
    /**
     * Helper method to get cache by name and validate it exists.
     *
     * @param cacheName Name of the cache
     * @return Cache instance or null if not found
     */
    private Cache getCache(String cacheName) {
        if (cacheName == null || cacheName.trim().isEmpty()) {
            logger.warning("Cache name cannot be null or empty");
            return null;
        }
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warning("Cache not found: " + cacheName);
        }
        return cache;
    }
    
    /**
     * Get the default time-to-live for cache entries.
     *
     * @return Default TTL in seconds
     */
    public long getDefaultTtl() {
        return defaultTtl;
    }
    
    /**
     * Convert TTL duration to the specified time unit.
     * 
     * @param targetUnit Target time unit
     * @return TTL in the specified unit
     */
    public long getDefaultTtl(TimeUnit targetUnit) {
        return targetUnit.convert(defaultTtl, TimeUnit.SECONDS);
    }

    // TODO: Add support for custom TTL per cache entry
    // FIXME: Implement cache statistics and monitoring
}