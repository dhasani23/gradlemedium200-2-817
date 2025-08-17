package com.gradlemedium200.orderservice.exception;

/**
 * Custom exception class for payment-related errors in the order processing system.
 * This exception is thrown when payment operations fail or encounter issues.
 * It contains additional context about the payment including error codes and payment IDs.
 * 
 * @author gradlemedium200
 */
public class PaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    private String paymentId;

    /**
     * Constructs a new PaymentException with the specified detail message.
     * The errorCode is set to "UNKNOWN" and paymentId is set to null.
     *
     * @param message the detail message for this exception
     */
    public PaymentException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
        this.paymentId = null;
    }

    /**
     * Constructs a new PaymentException with the specified detail message and payment ID.
     * The errorCode is set to "UNKNOWN".
     *
     * @param message the detail message for this exception
     * @param paymentId the ID of the payment that caused the exception
     */
    public PaymentException(String message, String paymentId) {
        super(message);
        this.errorCode = "UNKNOWN";
        this.paymentId = paymentId;
    }
    
    /**
     * Constructs a new PaymentException with the specified detail message, error code, and payment ID.
     *
     * @param message the detail message for this exception
     * @param errorCode specific error code for the payment error
     * @param paymentId the ID of the payment that caused the exception
     */
    public PaymentException(String message, String errorCode, String paymentId) {
        super(message);
        this.errorCode = errorCode;
        this.paymentId = paymentId;
    }
    
    /**
     * Constructs a new PaymentException with the specified detail message and cause.
     * The errorCode is set to "UNKNOWN" and paymentId is set to null.
     *
     * @param message the detail message for this exception
     * @param cause the cause of this exception
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN";
        this.paymentId = null;
    }

    /**
     * Constructs a new PaymentException with the specified detail message, payment ID, and cause.
     * The errorCode is set to "UNKNOWN".
     *
     * @param message the detail message for this exception
     * @param paymentId the ID of the payment that caused the exception
     * @param cause the cause of this exception
     */
    public PaymentException(String message, String paymentId, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN";
        this.paymentId = paymentId;
    }
    
    /**
     * Constructs a new PaymentException with the specified detail message, cause, error code, and payment ID.
     *
     * @param message the detail message for this exception
     * @param cause the cause of this exception
     * @param errorCode specific error code for the payment error
     * @param paymentId the ID of the payment that caused the exception
     */
    public PaymentException(String message, Throwable cause, String errorCode, String paymentId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.paymentId = paymentId;
    }

    /**
     * Returns the error code associated with this payment exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the payment ID associated with this payment exception.
     * 
     * @return the payment ID, may be null if no specific payment is associated
     */
    public String getPaymentId() {
        return paymentId;
    }
    
    /**
     * Sets the error code for this exception.
     * 
     * @param errorCode the error code to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Sets the payment ID for this exception.
     * 
     * @param paymentId the payment ID to set
     */
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append(" [errorCode=").append(errorCode).append(", paymentId=");
        if (paymentId != null) {
            builder.append(paymentId);
        } else {
            builder.append("unknown");
        }
        builder.append("]");
        return builder.toString();
    }
}