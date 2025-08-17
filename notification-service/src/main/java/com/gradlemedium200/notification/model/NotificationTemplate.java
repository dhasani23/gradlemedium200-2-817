package com.gradlemedium200.notification.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a template for notifications.
 * 
 * This class contains the structure and content of a notification template,
 * which can be used to generate actual notifications by substituting placeholders
 * with actual values.
 */
public class NotificationTemplate {
    
    private String id;
    private String name;
    private String type;
    private String subject;
    private String content;
    private Map<String, String> metadata;
    private String language;
    private String body;
    private String htmlBody;
    private boolean active;
    private Set<String> placeholders;
    
    /**
     * Default constructor.
     */
    public NotificationTemplate() {
        this.metadata = new HashMap<>();
        this.active = true;
    }
    
    /**
     * Constructs a notification template with essential fields.
     *
     * @param id Unique identifier for the template
     * @param name Human-readable name for the template
     * @param type Type of notification this template is for
     * @param subject Subject line or title for the notification
     * @param content Content of the notification with placeholders
     */
    public NotificationTemplate(String id, String name, String type, String subject, String content) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.subject = subject;
        this.content = content;
        this.metadata = new HashMap<>();
        this.active = true;
    }
    
    /**
     * Gets the template ID.
     *
     * @return The template ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the template ID.
     *
     * @param id The template ID to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Gets the template name.
     *
     * @return The template name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the template name.
     *
     * @param name The template name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the notification type for this template.
     *
     * @return The notification type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the notification type enum for this template.
     * Attempts to convert the string type to a NotificationType enum value.
     *
     * @return The notification type enum, or null if conversion fails
     */
    public NotificationType getTypeEnum() {
        if (type == null) {
            return null;
        }
        
        try {
            return NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Sets the notification type for this template.
     *
     * @param type The notification type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Sets the notification type for this template from an enum.
     *
     * @param type The notification type enum to set
     */
    public void setType(NotificationType type) {
        this.type = type != null ? type.name() : null;
    }
    
    /**
     * Gets the subject/title for notifications created from this template.
     *
     * @return The notification subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Sets the subject/title for notifications created from this template.
     *
     * @param subject The notification subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     * Gets the content with placeholders for this template.
     *
     * @return The notification content template
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content with placeholders for this template.
     *
     * @param content The notification content template to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Gets additional metadata for this template.
     *
     * @return The template metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Sets additional metadata for this template.
     *
     * @param metadata The template metadata to set
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    /**
     * Adds a single metadata key-value pair to this template.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Gets the language of this template.
     * 
     * @return The language code for this template
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Sets the language of this template.
     * 
     * @param language The language code to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * Gets the plain text body of the template.
     * 
     * @return The plain text body content
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Sets the plain text body of the template.
     * 
     * @param body The plain text body content to set
     */
    public void setBody(String body) {
        this.body = body;
    }
    
    /**
     * Gets the HTML formatted body of the template.
     * 
     * @return The HTML body content
     */
    public String getHtmlBody() {
        return htmlBody;
    }
    
    /**
     * Sets the HTML formatted body of the template.
     * 
     * @param htmlBody The HTML body content to set
     */
    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }
    
    /**
     * Checks if this template is active and available for use.
     * 
     * @return true if the template is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sets whether this template is active and available for use.
     * 
     * @param active The active status to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Gets the placeholders defined in this template.
     * 
     * @return The set of placeholder names
     */
    public Set<String> getPlaceholders() {
        return placeholders;
    }
    
    /**
     * Sets the placeholders for this template.
     * 
     * @param placeholders The set of placeholder names to set
     */
    public void setPlaceholders(Set<String> placeholders) {
        this.placeholders = placeholders;
    }
    
    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + (content != null ? (content.length() > 50 ? content.substring(0, 47) + "..." : content) : null) + '\'' +
                ", language='" + language + '\'' +
                ", active=" + active +
                ", metadata=" + metadata +
                '}';
    }
}