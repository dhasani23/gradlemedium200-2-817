package com.gradlemedium200.common.helper;

import com.gradlemedium200.common.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for message formatting and internationalization support.
 * Provides methods to retrieve localized messages and format message templates with parameters.
 */
@Component
public class MessageHelper {
    
    private final MessageSource messageSource;
    private final Locale defaultLocale;
    
    // Pattern to match placeholders like ${paramName} in message templates
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    
    /**
     * Constructor for MessageHelper
     * 
     * @param messageSource Spring message source for internationalization
     */
    public MessageHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
        // Default to US English locale
        this.defaultLocale = Locale.US;
    }
    
    /**
     * Constructor for MessageHelper with custom default locale
     * 
     * @param messageSource Spring message source for internationalization
     * @param defaultLocale Default locale for message resolution
     */
    public MessageHelper(MessageSource messageSource, Locale defaultLocale) {
        this.messageSource = messageSource;
        this.defaultLocale = defaultLocale;
    }
    
    /**
     * Get message by key using default locale
     * 
     * @param key Message key in the resource bundle
     * @return Localized message text, or the key itself if not found
     */
    public String getMessage(String key) {
        return getMessage(key, defaultLocale);
    }
    
    /**
     * Get message by key for specific locale
     * 
     * @param key Message key in the resource bundle
     * @param locale The locale to use for message lookup
     * @return Localized message text, or the key itself if not found
     */
    public String getMessage(String key, Locale locale) {
        if (StringUtil.isEmpty(key)) {
            return "";
        }
        
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            // FIXME: Consider adding logging here to track missing message keys
            return key; // Return key as fallback
        }
    }
    
    /**
     * Get formatted message by key with arguments
     * 
     * @param key Message key in the resource bundle
     * @param args Arguments to be inserted into the message template
     * @return Formatted message with arguments inserted
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, defaultLocale);
    }
    
    /**
     * Get formatted message by key with arguments for a specific locale
     * 
     * @param key Message key in the resource bundle
     * @param args Arguments to be inserted into the message template
     * @param locale The locale to use for message lookup
     * @return Formatted message with arguments inserted
     */
    public String getMessage(String key, Object[] args, Locale locale) {
        if (StringUtil.isEmpty(key)) {
            return "";
        }
        
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            // TODO: Add logging for missing message keys
            // Fall back to a simple concatenation of the key and args if message not found
            if (args != null && args.length > 0) {
                StringBuilder fallbackMessage = new StringBuilder(key);
                fallbackMessage.append(" (");
                for (int i = 0; i < args.length; i++) {
                    fallbackMessage.append(args[i]);
                    if (i < args.length - 1) {
                        fallbackMessage.append(", ");
                    }
                }
                fallbackMessage.append(")");
                return fallbackMessage.toString();
            }
            return key;
        }
    }
    
    /**
     * Format message template with named parameters
     * Replaces placeholders like ${name} with values from the parameters map
     * 
     * @param template Message template with named placeholders
     * @param parameters Map of parameter names to values
     * @return Formatted message with parameter values inserted
     */
    public String formatMessage(String template, Map<String, Object> parameters) {
        if (StringUtil.isEmpty(template)) {
            return "";
        }
        
        if (parameters == null || parameters.isEmpty()) {
            return template;
        }
        
        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Append text before the placeholder
            result.append(template, lastEnd, matcher.start());
            
            // Get the parameter name
            String paramName = matcher.group(1);
            
            // Replace placeholder with parameter value or keep placeholder if parameter not found
            Object value = parameters.get(paramName);
            if (value != null) {
                result.append(value);
            } else {
                // Keep the original placeholder if parameter not found
                result.append("${").append(paramName).append("}");
            }
            
            lastEnd = matcher.end();
        }
        
        // Append the remainder of the template
        if (lastEnd < template.length()) {
            result.append(template.substring(lastEnd));
        }
        
        return result.toString();
    }
    
    /**
     * Check if message exists for given key and locale
     * 
     * @param key Message key to check
     * @param locale Locale to check for
     * @return true if a message exists, false otherwise
     */
    public boolean hasMessage(String key, Locale locale) {
        if (StringUtil.isEmpty(key) || locale == null) {
            return false;
        }
        
        try {
            String message = messageSource.getMessage(key, null, locale);
            return StringUtil.isNotEmpty(message);
        } catch (NoSuchMessageException e) {
            return false;
        }
    }
    
    /**
     * Check if message exists for given key using default locale
     * 
     * @param key Message key to check
     * @return true if a message exists, false otherwise
     */
    public boolean hasMessage(String key) {
        return hasMessage(key, defaultLocale);
    }
    
    /**
     * Get the default locale used by this helper
     * 
     * @return The default locale
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }
}