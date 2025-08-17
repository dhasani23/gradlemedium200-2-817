package com.gradlemedium200.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data transfer object for payment responses.
 * Contains information about payment transactions received from payment gateways.
 */
public class PaymentResponseDto {

    private String paymentId;           // Unique payment transaction identifier
    private String orderId;             // Order identifier for payment
    private String status;              // Payment status (SUCCESS, FAILED, PENDING)
    private BigDecimal amount;          // Payment amount
    private String currency;            // Payment currency
    private LocalDateTime transactionDate; // Payment transaction timestamp
    private String errorMessage;        // Error message if payment failed
    private String errorCode;           // Error code if payment failed

    /**
     * Default constructor
     */
    public PaymentResponseDto() {
    }

    /**
     * Parameterized constructor with all fields
     * 
     * @param paymentId Unique payment transaction identifier
     * @param orderId Order identifier for payment
     * @param status Payment status
     * @param amount Payment amount
     * @param currency Payment currency
     * @param transactionDate Payment transaction timestamp
     * @param errorMessage Error message if payment failed
     * @param errorCode Error code if payment failed
     */
    public PaymentResponseDto(String paymentId, String orderId, String status, 
                             BigDecimal amount, String currency, 
                             LocalDateTime transactionDate, String errorMessage, String errorCode) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    
    /**
     * Parameterized constructor with essential fields
     * 
     * @param paymentId Unique payment transaction identifier
     * @param orderId Order identifier for payment
     * @param status Payment status
     * @param amount Payment amount
     * @param currency Payment currency
     * @param transactionDate Payment transaction timestamp
     * @param errorMessage Error message if payment failed
     */
    public PaymentResponseDto(String paymentId, String orderId, String status, 
                             BigDecimal amount, String currency, 
                             LocalDateTime transactionDate, String errorMessage) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.errorMessage = errorMessage;
    }

    /**
     * Checks if the payment was successful
     * 
     * @return true if payment status is SUCCESS, false otherwise
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(this.status);
    }

    // Getters and setters

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PaymentResponseDto that = (PaymentResponseDto) o;
        
        return Objects.equals(paymentId, that.paymentId) &&
               Objects.equals(orderId, that.orderId) &&
               Objects.equals(status, that.status) &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(currency, that.currency) &&
               Objects.equals(transactionDate, that.transactionDate) &&
               Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, orderId, status, amount, currency, transactionDate, errorMessage);
    }

    @Override
    public String toString() {
        return "PaymentResponseDto{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionDate=" + transactionDate +
                // Not including error message in toString to avoid potential sensitive information
                ", hasErrorMessage=" + (errorMessage != null && !errorMessage.isEmpty()) +
                '}';
    }
}