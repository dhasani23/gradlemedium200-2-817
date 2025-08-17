package com.gradlemedium200.userservice.model;

import java.time.LocalDateTime;

/**
 * Entity model representing notifications to be sent to users.
 */
public class Notification {
    
    /**
     * Enumeration of notification priority levels.
     */
    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }
    
    private String id;
    private String userId;
    private String subject;
    private String message;
    private String type; // EMAIL, SMS, PUSH
    private String status; // PENDING, SENT, QUEUED, FAILED, etc.
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer attempts;
    private String messageId; // AWS message ID
    private String errorMessage;
    
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
        this.attempts = 0;
    }
    
    public Notification(String userId, String subject, String message) {
        this();
        this.userId = userId;
        this.subject = subject;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                '}';
    }
}