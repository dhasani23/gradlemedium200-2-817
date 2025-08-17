package com.gradlemedium200.common.util;

import java.security.SecureRandom;

/**
 * Utility class for string manipulation, validation, and formatting.
 * This class provides static methods for common string operations.
 */
public final class StringUtil {
    
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private StringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Checks if a string is null or empty.
     *
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * Checks if a string is not null and not empty.
     *
     * @param str the string to check
     * @return true if the string is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is null, empty, or contains only whitespace, false otherwise
     */
    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Safely trims a string, handling null values.
     *
     * @param str the string to trim
     * @return the trimmed string, or null if the input was null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string, or null if the input was null
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * Generates a random alphanumeric string of the specified length.
     *
     * @param length the length of the string to generate
     * @return the generated random string
     * @throws IllegalArgumentException if length is negative
     */
    public static String generateRandomString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative");
        }
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            sb.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        
        return sb.toString();
    }
    
    /**
     * Masks sensitive data keeping only a specified number of visible characters.
     * The rest of the characters are replaced with asterisks.
     * 
     * @param input the string containing sensitive data
     * @param visibleChars the number of characters to leave visible (from the beginning)
     * @return the masked string, or null if the input was null
     */
    public static String maskSensitiveData(String input, int visibleChars) {
        if (input == null) {
            return null;
        }
        
        if (visibleChars < 0) {
            visibleChars = 0;
        }
        
        if (input.length() <= visibleChars) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        result.append(input.substring(0, visibleChars));
        
        // Append asterisks for the remaining characters
        for (int i = visibleChars; i < input.length(); i++) {
            result.append('*');
        }
        
        return result.toString();
    }
}