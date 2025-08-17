package com.gradlemedium200.notification.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Email-specific notification model with email-related properties including 
 * sender, recipient email, and HTML content support.
 * Extends the base Notification class with additional fields specific to email communications.
 * 
 * @author gradlemedium200
 */
public class EmailNotification extends Notification {
    
    /**
     * Sender's email address
     */
    private String fromEmail;
    
    /**
     * Recipient's email address
     */
    private String toEmail;
    
    /**
     * Carbon copy email addresses
     */
    private List<String> ccEmails;
    
    /**
     * Blind carbon copy email addresses
     */
    private List<String> bccEmails;
    
    /**
     * HTML formatted email content
     */
    private String htmlContent;
    
    /**
     * Email attachment file paths
     */
    private List<String> attachments;
    
    /**
     * Default constructor initializes list fields.
     */
    public EmailNotification() {
        super();
        this.ccEmails = new ArrayList<>();
        this.bccEmails = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.setType(NotificationType.EMAIL);
    }
    
    /**
     * Parameterized constructor for creating an email notification.
     *
     * @param id The unique identifier
     * @param recipientId The recipient's user ID
     * @param subject The email subject
     * @param message The plain text message content
     * @param fromEmail The sender's email address
     * @param toEmail The recipient's email address
     */
    public EmailNotification(String id, String recipientId, String subject, String message, 
                            String fromEmail, String toEmail) {
        super(id, recipientId, message, subject, NotificationType.EMAIL);
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.ccEmails = new ArrayList<>();
        this.bccEmails = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }
    
    /**
     * Gets the sender email address.
     *
     * @return The sender email address
     */
    public String getFromEmail() {
        return fromEmail;
    }
    
    /**
     * Sets the sender email address.
     *
     * @param fromEmail The sender email address
     */
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
    
    /**
     * Gets the recipient email address.
     *
     * @return The recipient email address
     */
    public String getToEmail() {
        return toEmail;
    }
    
    /**
     * Sets the recipient email address.
     *
     * @param toEmail The recipient email address
     */
    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
    
    /**
     * Gets the CC email addresses.
     *
     * @return List of CC email addresses
     */
    public List<String> getCcEmails() {
        return ccEmails;
    }
    
    /**
     * Sets the CC email addresses.
     *
     * @param ccEmails List of CC email addresses
     */
    public void setCcEmails(List<String> ccEmails) {
        this.ccEmails = ccEmails != null ? ccEmails : new ArrayList<>();
    }
    
    /**
     * Gets the BCC email addresses.
     *
     * @return List of BCC email addresses
     */
    public List<String> getBccEmails() {
        return bccEmails;
    }
    
    /**
     * Sets the BCC email addresses.
     *
     * @param bccEmails List of BCC email addresses
     */
    public void setBccEmails(List<String> bccEmails) {
        this.bccEmails = bccEmails != null ? bccEmails : new ArrayList<>();
    }
    
    /**
     * Gets the HTML content of the email.
     *
     * @return The HTML content
     */
    public String getHtmlContent() {
        return htmlContent;
    }
    
    /**
     * Sets the HTML content of the email.
     *
     * @param htmlContent The HTML content
     */
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    
    /**
     * Gets the list of attachment file paths.
     *
     * @return List of attachment file paths
     */
    public List<String> getAttachments() {
        return attachments;
    }
    
    /**
     * Sets the list of attachment file paths.
     *
     * @param attachments List of attachment file paths
     */
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }
    
    /**
     * Adds an attachment to the email.
     *
     * @param attachmentPath The file path of the attachment to add
     */
    public void addAttachment(String attachmentPath) {
        if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
            if (this.attachments == null) {
                this.attachments = new ArrayList<>();
            }
            this.attachments.add(attachmentPath);
        } else {
            // TODO: Consider logging a warning when null/empty attachment paths are provided
        }
    }
    
    /**
     * Determines if this email has any attachments.
     *
     * @return true if the email has attachments, false otherwise
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
    
    /**
     * Determines if this email has HTML content.
     *
     * @return true if HTML content is present, false otherwise
     */
    public boolean isHtml() {
        return htmlContent != null && !htmlContent.trim().isEmpty();
    }
    
    /**
     * Validates if the email has all required fields for sending.
     *
     * @return true if the email notification is valid for sending
     */
    public boolean isValid() {
        // FIXME: Add proper email address format validation using regex
        return fromEmail != null && !fromEmail.isEmpty() &&
               toEmail != null && !toEmail.isEmpty() &&
               (getMessage() != null || htmlContent != null);
    }
    
    /**
     * Returns a string representation of this email notification.
     *
     * @return A string representation of this email notification
     */
    @Override
    public String toString() {
        return "EmailNotification{" +
                "id='" + getId() + '\'' +
                ", recipientId='" + getRecipientId() + '\'' +
                ", subject='" + getSubject() + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", ccEmails=" + ccEmails.size() +
                ", bccEmails=" + bccEmails.size() +
                ", hasHtml=" + (htmlContent != null) +
                ", attachments=" + attachments.size() +
                ", status=" + getStatus() +
                '}';
    }
    
    /**
     * Compares this email notification to the specified object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmailNotification that = (EmailNotification) o;
        return Objects.equals(fromEmail, that.fromEmail) &&
               Objects.equals(toEmail, that.toEmail);
    }
    
    /**
     * Returns a hash code value for this email notification.
     *
     * @return A hash code value for this email notification
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromEmail, toEmail);
    }
}