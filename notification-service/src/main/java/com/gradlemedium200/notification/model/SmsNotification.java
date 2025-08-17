package com.gradlemedium200.notification.model;

import java.util.Objects;

/**
 * SMS-specific notification model with SMS-related properties including phone number 
 * and SMS service provider information.
 * 
 * This class extends the base Notification class and adds properties specific to 
 * SMS message delivery, such as recipient phone number, country code, sender ID,
 * and message classification.
 * 
 * @author gradlemedium200
 */
public class SmsNotification extends Notification {

    /**
     * Recipient's phone number without country code
     */
    private String phoneNumber;
    
    /**
     * Country code for the phone number (e.g., +1 for US)
     */
    private String countryCode;
    
    /**
     * SMS sender ID or shortcode that appears as the sender
     */
    private String senderId;
    
    /**
     * Type of SMS message (promotional or transactional)
     * Affects delivery priority and regulatory compliance requirements
     */
    private String messageType;

    /**
     * Default constructor initializing an SMS notification with default values
     */
    public SmsNotification() {
        super();
        // Default country code if none is provided
        this.countryCode = "+1";
        // Default to transactional message type for higher priority delivery
        this.messageType = "transactional";
    }

    /**
     * Parameterized constructor for creating an SMS notification with essential fields
     *
     * @param id The unique identifier
     * @param recipientId The recipient's identifier
     * @param message The notification message content
     * @param subject The notification subject
     * @param type The notification type
     * @param phoneNumber The recipient's phone number
     * @param countryCode The country code for the phone number
     * @param senderId The SMS sender ID
     * @param messageType The type of SMS message
     */
    public SmsNotification(String id, String recipientId, String message, String subject, 
                          NotificationType type, String phoneNumber, String countryCode,
                          String senderId, String messageType) {
        super(id, recipientId, message, subject, type);
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.senderId = senderId;
        this.messageType = messageType;
    }

    /**
     * Gets the recipient phone number
     *
     * @return The recipient phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the recipient phone number
     *
     * @param phoneNumber The recipient phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the country code
     *
     * @return The country code
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the country code
     *
     * @param countryCode The country code
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Gets the complete phone number with country code
     * Formats the phone number by combining country code and phone number
     *
     * @return The complete phone number with country code
     */
    public String getFullPhoneNumber() {
        // Ensure country code is properly formatted with + prefix
        String formattedCountryCode = countryCode;
        if (countryCode != null && !countryCode.isEmpty() && !countryCode.startsWith("+")) {
            formattedCountryCode = "+" + countryCode;
        }
        
        // Handle case where either component is null or empty
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return formattedCountryCode;
        }
        
        if (formattedCountryCode == null || formattedCountryCode.isEmpty()) {
            return phoneNumber;
        }
        
        // Remove any leading zeros from phone number when combining with country code
        String formattedPhoneNumber = phoneNumber;
        if (phoneNumber.startsWith("0")) {
            formattedPhoneNumber = phoneNumber.replaceFirst("^0+", "");
        }
        
        return formattedCountryCode + formattedPhoneNumber;
    }

    /**
     * Gets the SMS sender ID
     *
     * @return The SMS sender ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets the SMS sender ID
     *
     * @param senderId The SMS sender ID
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the message type (promotional/transactional)
     * 
     * @return The message type
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Sets the message type (promotional/transactional)
     * 
     * @param messageType The message type
     */
    public void setMessageType(String messageType) {
        if (messageType != null && 
            (messageType.equalsIgnoreCase("promotional") || 
             messageType.equalsIgnoreCase("transactional"))) {
            this.messageType = messageType.toLowerCase();
        } else {
            // FIXME: Consider throwing an IllegalArgumentException instead of defaulting
            this.messageType = "transactional";
        }
    }
    
    /**
     * Determines if this is a promotional message
     * 
     * @return true if this is a promotional message
     */
    public boolean isPromotional() {
        return "promotional".equalsIgnoreCase(messageType);
    }
    
    /**
     * Determines if this is a transactional message
     * 
     * @return true if this is a transactional message
     */
    public boolean isTransactional() {
        return "transactional".equalsIgnoreCase(messageType);
    }
    
    /**
     * Validates whether the SMS notification has all required fields for sending
     * 
     * @return true if the notification is valid for sending
     */
    public boolean isValid() {
        // Check that we have a valid phone number and message content
        return phoneNumber != null && !phoneNumber.isEmpty() && 
               getMessage() != null && !getMessage().isEmpty();
    }

    /**
     * Returns a string representation of this SMS notification.
     *
     * @return A string representation of this SMS notification
     */
    @Override
    public String toString() {
        return "SmsNotification{" +
                "id='" + getId() + '\'' +
                ", recipientId='" + getRecipientId() + '\'' +
                ", phoneNumber='" + getFullPhoneNumber() + '\'' +
                ", senderId='" + senderId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", status=" + getStatus() +
                ", type=" + getType() +
                '}';
    }

    /**
     * Compares this SMS notification to the specified object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SmsNotification that = (SmsNotification) o;
        return Objects.equals(phoneNumber, that.phoneNumber) && 
               Objects.equals(countryCode, that.countryCode) &&
               Objects.equals(senderId, that.senderId);
    }

    /**
     * Returns a hash code value for this SMS notification.
     *
     * @return A hash code value for this SMS notification
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), phoneNumber, countryCode, senderId);
    }
}