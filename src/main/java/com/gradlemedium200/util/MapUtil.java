package com.gradlemedium200.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating and manipulating maps.
 */
public class MapUtil {

    /**
     * Creates a map with one key-value pair.
     *
     * @param key1 the first key
     * @param value1 the first value
     * @param <K> the key type
     * @param <V> the value type
     * @return a map containing the key-value pair
     */
    public static <K, V> Map<K, V> of(K key1, V value1) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        return map;
    }

    /**
     * Creates a map with two key-value pairs.
     *
     * @param key1 the first key
     * @param value1 the first value
     * @param key2 the second key
     * @param value2 the second value
     * @param <K> the key type
     * @param <V> the value type
     * @return a map containing the key-value pairs
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Creates a map with three key-value pairs.
     *
     * @param key1 the first key
     * @param value1 the first value
     * @param key2 the second key
     * @param value2 the second value
     * @param key3 the third key
     * @param value3 the third value
     * @param <K> the key type
     * @param <V> the value type
     * @return a map containing the key-value pairs
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Creates a map with four key-value pairs.
     *
     * @param key1 the first key
     * @param value1 the first value
     * @param key2 the second key
     * @param value2 the second value
     * @param key3 the third key
     * @param value3 the third value
     * @param key4 the fourth key
     * @param value4 the fourth value
     * @param <K> the key type
     * @param <V> the value type
     * @return a map containing the key-value pairs
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }
}