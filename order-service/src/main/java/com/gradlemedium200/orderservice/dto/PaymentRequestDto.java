package com.gradlemedium200.orderservice.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for payment requests.
 * This DTO contains all necessary information for processing a payment for an order.
 */
public class PaymentRequestDto {
    
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private Map<String, String> paymentDetails;
    
    /**
     * Default constructor initializing empty payment details map.
     */
    public PaymentRequestDto() {
        this.paymentDetails = new HashMap<>();
    }
    
    /**
     * Parameterized constructor.
     * 
     * @param orderId The order identifier
     * @param customerId The customer identifier
     * @param amount The payment amount
     * @param currency The payment currency
     * @param paymentMethod The payment method (card, bank transfer, etc.)
     * @param paymentDetails Additional payment details
     */
    public PaymentRequestDto(String orderId, String customerId, BigDecimal amount, String currency, 
                           String paymentMethod, Map<String, String> paymentDetails) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails != null ? paymentDetails : new HashMap<>();
    }
    
    /**
     * Validates the payment request data.
     * Checks for required fields and valid values.
     * 
     * @return true if the payment request is valid, false otherwise
     */
    public boolean validate() {
        // Check for null or empty required fields
        if (isEmpty(orderId) || isEmpty(customerId) || amount == null) {
            return false;
        }
        
        // Check for valid amount (positive value)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Check for valid currency
        if (isEmpty(currency)) {
            return false;
        }
        
        // Check for valid payment method
        if (isEmpty(paymentMethod)) {
            return false;
        }
        
        // Payment method specific validation
        switch (paymentMethod.toLowerCase()) {
            case "card":
                return validateCardPayment();
            case "bank transfer":
                return validateBankTransfer();
            default:
                // For other payment methods, just ensure payment details are provided
                return paymentDetails != null && !paymentDetails.isEmpty();
        }
    }
    
    /**
     * Helper method to validate credit card payment details.
     * 
     * @return true if card payment details are valid, false otherwise
     */
    private boolean validateCardPayment() {
        // FIXME: Implement proper card validation (Luhn algorithm check)
        return paymentDetails != null && 
               paymentDetails.containsKey("cardNumber") && 
               paymentDetails.containsKey("expiryDate") && 
               paymentDetails.containsKey("cvv");
    }
    
    /**
     * Helper method to validate bank transfer details.
     * 
     * @return true if bank transfer details are valid, false otherwise
     */
    private boolean validateBankTransfer() {
        return paymentDetails != null && 
               paymentDetails.containsKey("accountNumber") && 
               paymentDetails.containsKey("bankCode");
    }
    
    /**
     * Helper method to check if a string is null or empty.
     * 
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // Getters and Setters
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Map<String, String> getPaymentDetails() {
        return paymentDetails;
    }
    
    public void setPaymentDetails(Map<String, String> paymentDetails) {
        this.paymentDetails = paymentDetails != null ? paymentDetails : new HashMap<>();
    }
    
    /**
     * Add a specific payment detail.
     * 
     * @param key The detail key
     * @param value The detail value
     */
    public void addPaymentDetail(String key, String value) {
        if (this.paymentDetails == null) {
            this.paymentDetails = new HashMap<>();
        }
        this.paymentDetails.put(key, value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequestDto that = (PaymentRequestDto) o;
        return Objects.equals(orderId, that.orderId) &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(currency, that.currency) &&
               Objects.equals(paymentMethod, that.paymentMethod) &&
               Objects.equals(paymentDetails, that.paymentDetails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId, amount, currency, paymentMethod, paymentDetails);
    }
    
    /**
     * Convenience method to get the card number from payment details.
     * 
     * @return The card number or null if not present
     */
    public String getCardNumber() {
        if (paymentDetails != null) {
            return paymentDetails.get("cardNumber");
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "PaymentRequestDto{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentDetails=" + paymentDetails +
                '}';
    }
}