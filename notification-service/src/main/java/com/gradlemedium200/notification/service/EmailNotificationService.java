package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.aws.SnsPublisher;
import com.gradlemedium200.notification.model.EmailNotification;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.model.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service responsible for handling email notification delivery through AWS SNS and SES integration
 * with template processing and attachment support.
 *
 * <p>This service implements the NotificationChannel interface and is specifically designed
 * to handle email notifications, providing validation, formatting, and delivery capabilities.</p>
 *
 * @author gradlemedium200
 */
@Service
public class EmailNotificationService implements NotificationChannel {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    private final SnsPublisher snsPublisher;
    
    @Value("${notification.email.from:no-reply@gradlemedium200.com}")
    private String fromEmail;
    
    @Value("${notification.email.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${notification.email.enabled:true}")
    private boolean enabled;
    
    @Autowired
    public EmailNotificationService(SnsPublisher snsPublisher) {
        this.snsPublisher = snsPublisher;
    }
    
    /**
     * Sends an email notification through SNS.
     * <p>
     * This method handles the delivery of notifications by converting general notifications
     * to email-specific ones if needed, and then publishing them through AWS SNS.
     * </p>
     *
     * @param notification The notification to be sent
     * @return true if the notification was successfully sent, false otherwise
     */
    @Override
    public boolean send(Notification notification) {
        if (!enabled) {
            logger.warn("Email notification channel is disabled. Notification not sent: {}", notification.getId());
            return false;
        }
        
        if (!validate(notification)) {
            logger.error("Invalid email notification: {}", notification.getId());
            notification.setStatus(NotificationStatus.INVALID);
            return false;
        }
        
        // Convert to EmailNotification if it's not already one
        EmailNotification emailNotification;
        if (notification instanceof EmailNotification) {
            emailNotification = (EmailNotification) notification;
        } else {
            logger.debug("Converting general notification to email notification");
            emailNotification = new EmailNotification();
            emailNotification.setId(notification.getId());
            emailNotification.setSubject(notification.getSubject());
            emailNotification.setMessage(notification.getMessage());
            emailNotification.setFromEmail(fromEmail);
            emailNotification.setToEmail(notification.getRecipientId() + "@example.com"); // Default transform
            // TODO: Implement proper recipient ID to email address resolution
        }
        
        return sendEmailNotification(emailNotification);
    }
    
    /**
     * Validates that the notification can be sent as an email.
     * <p>
     * Ensures the notification contains all required information and meets
     * the format requirements for email delivery.
     * </p>
     *
     * @param notification The notification to validate
     * @return true if the notification is valid for email delivery, false otherwise
     */
    @Override
    public boolean validate(Notification notification) {
        if (notification == null) {
            logger.error("Notification is null");
            return false;
        }
        
        if (notification instanceof EmailNotification) {
            EmailNotification emailNotification = (EmailNotification) notification;
            return validateEmailNotification(emailNotification);
        }
        
        // For general notifications, check basic requirements
        return notification.getSubject() != null && !notification.getSubject().isEmpty() &&
               notification.getMessage() != null && !notification.getMessage().isEmpty() &&
               notification.getRecipientId() != null && !notification.getRecipientId().isEmpty();
    }
    
    /**
     * Validates an email notification by checking all required fields and formats.
     * 
     * @param emailNotification The email notification to validate
     * @return true if the email notification is valid, false otherwise
     */
    private boolean validateEmailNotification(EmailNotification emailNotification) {
        // Check required fields
        if (emailNotification.getSubject() == null || emailNotification.getSubject().isEmpty()) {
            logger.error("Email notification missing subject: {}", emailNotification.getId());
            return false;
        }
        
        // Either message or HTML content must be present
        if ((emailNotification.getMessage() == null || emailNotification.getMessage().isEmpty()) && 
            (emailNotification.getHtmlContent() == null || emailNotification.getHtmlContent().isEmpty())) {
            logger.error("Email notification missing both plain text and HTML content: {}", emailNotification.getId());
            return false;
        }
        
        // Validate email addresses
        if (!validateEmailAddress(emailNotification.getFromEmail())) {
            logger.error("Invalid from email address: {}", emailNotification.getFromEmail());
            return false;
        }
        
        if (!validateEmailAddress(emailNotification.getToEmail())) {
            logger.error("Invalid to email address: {}", emailNotification.getToEmail());
            return false;
        }
        
        // Validate CC and BCC email addresses if present
        if (emailNotification.getCcEmails() != null) {
            for (String ccEmail : emailNotification.getCcEmails()) {
                if (!validateEmailAddress(ccEmail)) {
                    logger.error("Invalid CC email address: {}", ccEmail);
                    return false;
                }
            }
        }
        
        if (emailNotification.getBccEmails() != null) {
            for (String bccEmail : emailNotification.getBccEmails()) {
                if (!validateEmailAddress(bccEmail)) {
                    logger.error("Invalid BCC email address: {}", bccEmail);
                    return false;
                }
            }
        }
        
        // Validate attachments if present
        if (emailNotification.hasAttachments()) {
            for (String attachment : emailNotification.getAttachments()) {
                if (attachment == null || attachment.isEmpty()) {
                    logger.error("Invalid attachment path in email: {}", emailNotification.getId());
                    return false;
                }
                // TODO: Check if attachment file exists and is readable
            }
        }
        
        return true;
    }
    
    /**
     * Sends a specific email notification.
     * <p>
     * Prepares the email content, sets up any necessary attributes, and publishes it through SNS.
     * </p>
     *
     * @param emailNotification The email notification to send
     * @return true if the notification was successfully sent, false otherwise
     */
    public boolean sendEmailNotification(EmailNotification emailNotification) {
        try {
            logger.info("Sending email notification: {} to {}", emailNotification.getId(), emailNotification.getToEmail());
            
            // Set default from address if not specified
            if (emailNotification.getFromEmail() == null || emailNotification.getFromEmail().isEmpty()) {
                emailNotification.setFromEmail(fromEmail);
            }
            
            // Prepare email content (HTML or plain text)
            String content = prepareEmailContent(emailNotification);
            
            // Setup any additional attributes needed for SNS
            Map<String, String> attributes = new HashMap<>();
            attributes.put("email-type", emailNotification.isHtml() ? "html" : "text");
            
            // Add recipient information
            attributes.put("recipient", emailNotification.getToEmail());
            
            if (emailNotification.getCcEmails() != null && !emailNotification.getCcEmails().isEmpty()) {
                attributes.put("cc", String.join(",", emailNotification.getCcEmails()));
            }
            
            if (emailNotification.getBccEmails() != null && !emailNotification.getBccEmails().isEmpty()) {
                attributes.put("bcc", String.join(",", emailNotification.getBccEmails()));
            }
            
            // Handle attachments
            if (emailNotification.hasAttachments()) {
                // TODO: Implement attachment handling mechanism
                // For SNS with SES integration, attachments might require a different approach
                // like storing them in S3 and including links
                attributes.put("has-attachments", "true");
                attributes.put("attachment-count", String.valueOf(emailNotification.getAttachments().size()));
                logger.warn("Email attachments feature not fully implemented: {}", emailNotification.getId());
            }
            
            // Publish the message to SNS
            String messageId = snsPublisher.publishWithAttributes(
                content,
                snsPublisher.publishEmailMessage(
                    emailNotification.getSubject(), 
                    content, 
                    null
                ),
                attributes
            );
            
            // Update notification status
            if (messageId != null && !messageId.isEmpty()) {
                emailNotification.setStatus(NotificationStatus.SENT);
                logger.info("Email notification sent successfully: {} with SNS message ID: {}", 
                    emailNotification.getId(), messageId);
                return true;
            } else {
                emailNotification.setStatus(NotificationStatus.FAILED);
                logger.error("Failed to send email notification: {}", emailNotification.getId());
                return false;
            }
        } catch (Exception e) {
            emailNotification.setStatus(NotificationStatus.FAILED);
            emailNotification.incrementRetryCount();
            logger.error("Exception while sending email notification {}: {}", emailNotification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validates an email address format.
     * <p>
     * Uses regex pattern matching to ensure the email address conforms to standard format.
     * </p>
     *
     * @param emailAddress The email address to validate
     * @return true if the email address is valid, false otherwise
     */
    public boolean validateEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(emailAddress).matches();
    }
    
    /**
     * Prepares email content including HTML formatting.
     * <p>
     * Decides between HTML and plain text content and applies any necessary 
     * formatting or template processing.
     * </p>
     *
     * @param emailNotification The email notification to prepare content for
     * @return The prepared email content as a string
     */
    public String prepareEmailContent(EmailNotification emailNotification) {
        if (emailNotification.isHtml()) {
            return emailNotification.getHtmlContent();
        } 
        
        // If there's no HTML content, use plain text message
        if (emailNotification.getMessage() != null && !emailNotification.getMessage().isEmpty()) {
            // Simple conversion of plain text to basic HTML if needed
            // This could be expanded for more sophisticated template processing
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html><html><body>");
            
            // Convert plain text to HTML paragraphs
            String[] paragraphs = emailNotification.getMessage().split("\n\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    htmlBuilder.append("<p>").append(paragraph.replace("\n", "<br/>")).append("</p>");
                }
            }
            
            htmlBuilder.append("</body></html>");
            return htmlBuilder.toString();
        }
        
        // Fallback case
        return "<p>No content provided</p>";
    }
    
    /**
     * Returns the name of the email channel.
     *
     * @return The name "email" as the channel identifier
     */
    @Override
    public String getChannelName() {
        return "email";
    }
    
    /**
     * Checks if the email notification channel is enabled.
     *
     * @return true if email notifications are enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Gets the maximum number of retry attempts for failed email notifications.
     *
     * @return The maximum retry attempts configured for this service
     */
    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    /**
     * Returns the priority level of the email channel.
     * Email is considered a medium priority channel.
     * 
     * @return Priority level 5 (medium priority)
     */
    @Override
    public int getPriority() {
        return 5;
    }
}