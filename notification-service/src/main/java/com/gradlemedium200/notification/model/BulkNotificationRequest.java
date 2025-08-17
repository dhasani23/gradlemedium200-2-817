package com.gradlemedium200.notification.model;

import java.util.List;
import java.util.Map;

/**
 * Model class representing a request to send notifications to multiple recipients.
 * Used for bulk notification operations through the notification service.
 */
public class BulkNotificationRequest {
    
    private List<String> recipientIds;
    private NotificationType type;
    private String message;
    private String subject;
    private Map<String, String> variables;
    private boolean useTemplate;
    private String templateId;
    
    /**
     * Default constructor
     */
    public BulkNotificationRequest() {
    }
    
    /**
     * Constructor with essential fields for direct message sending
     * 
     * @param recipientIds List of recipient IDs
     * @param message The notification message
     * @param subject The notification subject
     */
    public BulkNotificationRequest(List<String> recipientIds, String message, String subject) {
        this.recipientIds = recipientIds;
        this.message = message;
        this.subject = subject;
        this.useTemplate = false;
    }
    
    /**
     * Constructor for template-based notifications
     * 
     * @param recipientIds List of recipient IDs
     * @param type The notification type
     * @param variables Template variables for placeholder substitution
     * @param useTemplate Whether to use a template
     */
    public BulkNotificationRequest(List<String> recipientIds, NotificationType type, 
            Map<String, String> variables, boolean useTemplate) {
        this.recipientIds = recipientIds;
        this.type = type;
        this.variables = variables;
        this.useTemplate = useTemplate;
    }

    /**
     * @return the list of recipient IDs
     */
    public List<String> getRecipientIds() {
        return recipientIds;
    }

    /**
     * @param recipientIds the list of recipient IDs to set
     */
    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }

    /**
     * @return the notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * @param type the notification type to set
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * @return the notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the notification message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the notification subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the notification subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the template variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * @param variables the template variables to set
     */
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * @return whether to use a template
     */
    public boolean isUseTemplate() {
        return useTemplate;
    }

    /**
     * @param useTemplate whether to use a template
     */
    public void setUseTemplate(boolean useTemplate) {
        this.useTemplate = useTemplate;
    }

    /**
     * @return the template ID
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * @param templateId the template ID to set
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        return "BulkNotificationRequest [recipientIds=" + recipientIds + 
               ", type=" + type + 
               ", useTemplate=" + useTemplate + 
               ", templateId=" + templateId + "]";
    }
}