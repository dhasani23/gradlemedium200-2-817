package com.gradlemedium200.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * Utility class for JSON serialization and deserialization operations.
 * Provides methods to convert between Java objects and JSON strings.
 */
public final class JsonUtil {

    /**
     * Jackson ObjectMapper instance configured for JSON operations.
     * ObjectMapper is thread-safe once configured.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Configure ObjectMapper
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param object The object to convert to JSON
     * @return String JSON representation of the object
     * @throws IllegalArgumentException if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts a JSON string to an object of the specified type.
     *
     * @param json  The JSON string to convert
     * @param clazz The class of the target object
     * @param <T>   The type of the target object
     * @return The converted object
     * @throws IllegalArgumentException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Checks if a string is valid JSON.
     *
     * @param json The string to check
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Attempt to parse the JSON
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Converts an object to a pretty-formatted JSON string.
     *
     * @param object The object to convert
     * @return A pretty-formatted JSON string
     * @throws IllegalArgumentException if serialization fails
     */
    public static String toPrettyJson(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert object to pretty JSON", e);
        }
    }

    /**
     * Creates a deep copy of an object using JSON serialization.
     * This method is useful when you need a completely detached copy of an object.
     *
     * @param source      The source object to copy
     * @param targetClass The class of the target object
     * @param <T>         The type of the target object
     * @return A new instance of the target class with copied data
     * @throws IllegalArgumentException if the copy operation fails
     * @throws IllegalStateException if source is null
     */
    public static <T> T copyObject(Object source, Class<T> targetClass) {
        if (source == null) {
            throw new IllegalStateException("Cannot copy null object");
        }
        
        try {
            // Serialize and deserialize to create a deep copy
            String json = OBJECT_MAPPER.writeValueAsString(source);
            return OBJECT_MAPPER.readValue(json, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to copy object", e);
        }
    }

    // FIXME: Add support for generic types with TypeReference
    
    // TODO: Consider adding methods for array/collection deserialization
    
    // TODO: Add error handling for specific JSON parsing exceptions
}