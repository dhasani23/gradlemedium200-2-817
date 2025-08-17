package com.gradlemedium200.orderservice.service;

import com.gradlemedium200.orderservice.dto.PaymentRequestDto;
import com.gradlemedium200.orderservice.dto.PaymentResponseDto;
import com.gradlemedium200.orderservice.exception.PaymentException;
import com.gradlemedium200.orderservice.integration.PaymentGatewayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for coordinating payment processing and payment status management.
 * Provides business logic layer for payment operations and caches payment status.
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentGatewayClient paymentGatewayClient;
    
    // Simple cache for payment statuses to reduce redundant external calls
    // In a production environment, consider using a distributed cache like Redis
    private final Map<String, String> paymentStatusCache = new ConcurrentHashMap<>();

    @Autowired
    public PaymentService(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    /**
     * Processes a payment request through the external payment gateway
     *
     * @param paymentRequest The payment details to process
     * @return Payment response with transaction results
     * @throws PaymentException if payment processing fails
     */
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequest) {
        logger.info("Processing payment for order: {}", paymentRequest.getOrderId());
        
        // Validate request
        validatePaymentRequest(paymentRequest);
        
        try {
            // Call payment gateway
            PaymentResponseDto response = paymentGatewayClient.processPayment(paymentRequest);
            
            if (response == null) {
                throw new PaymentException("Null response received from payment gateway");
            }
            
            // Process response
            if (response.isSuccessful()) {
                // Cache successful payment status
                if (response.getPaymentId() != null) {
                    paymentStatusCache.put(response.getPaymentId(), response.getStatus());
                }
                
                logger.info("Payment successful for order {}, payment ID: {}", 
                        paymentRequest.getOrderId(), response.getPaymentId());
            } else {
                logger.warn("Payment failed for order {}: {} - {}", 
                        paymentRequest.getOrderId(), 
                        response.getErrorCode(), 
                        response.getErrorMessage());
                
                // For failed payments, we might want to handle specific error codes differently
                if ("INSUFFICIENT_FUNDS".equals(response.getErrorCode())) {
                    // Special handling for insufficient funds
                    logger.warn("Customer has insufficient funds");
                }
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Error occurred while processing payment for order: {}", 
                    paymentRequest.getOrderId(), e);
            throw new PaymentException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if a payment is valid and legitimate
     *
     * @param paymentId ID of the payment to validate
     * @return true if payment is valid, false otherwise
     */
    public boolean validatePayment(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            logger.warn("Attempted to validate null or empty payment ID");
            return false;
        }
        
        logger.info("Validating payment ID: {}", paymentId);
        
        try {
            return paymentGatewayClient.validatePayment(paymentId);
        } catch (Exception e) {
            logger.error("Error validating payment ID: {}", paymentId, e);
            return false;
        }
    }

    /**
     * Processes a refund for a previously successful payment
     *
     * @param paymentId ID of the payment to refund
     * @param amount Amount to refund
     * @return Refund transaction result
     * @throws PaymentException if refund processing fails
     */
    public PaymentResponseDto refundPayment(String paymentId, BigDecimal amount) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new PaymentException("Payment ID cannot be null or empty");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Refund amount must be positive", paymentId);
        }
        
        logger.info("Processing refund for payment ID: {}, amount: {}", paymentId, amount);
        
        // First validate that the payment exists and is valid for refund
        if (!validatePayment(paymentId)) {
            throw new PaymentException("Cannot refund invalid or non-existent payment", paymentId);
        }
        
        try {
            // Process refund through payment gateway
            PaymentResponseDto response = paymentGatewayClient.processRefund(paymentId, amount);
            
            if (response.isSuccessful()) {
                logger.info("Refund successful for payment ID: {}, refund ID: {}", 
                        paymentId, response.getPaymentId());
                
                // Update cache if needed
                if (response.getPaymentId() != null) {
                    paymentStatusCache.put(response.getPaymentId(), response.getStatus());
                }
            } else {
                logger.warn("Refund failed for payment ID {}: {} - {}", 
                        paymentId, 
                        response.getErrorCode(), 
                        response.getErrorMessage());
                
                // Handle specific refund error cases
                if ("ALREADY_REFUNDED".equals(response.getErrorCode())) {
                    throw new PaymentException("Payment already refunded", response.getErrorCode(), paymentId);
                }
            }
            
            return response;
        } catch (PaymentException pe) {
            // Rethrow payment exceptions
            throw pe;
        } catch (Exception e) {
            logger.error("Error processing refund for payment ID: {}", paymentId, e);
            throw new PaymentException("Refund processing failed: " + e.getMessage(), paymentId, e);
        }
    }

    /**
     * Retrieves the current status of a payment
     *
     * @param paymentId ID of the payment to check
     * @return Current payment status
     * @throws PaymentException if status retrieval fails
     */
    public String getPaymentStatus(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new PaymentException("Payment ID cannot be null or empty");
        }
        
        logger.info("Retrieving payment status for ID: {}", paymentId);
        
        // Check cache first
        String cachedStatus = paymentStatusCache.get(paymentId);
        if (cachedStatus != null) {
            logger.debug("Retrieved payment status from cache for ID {}: {}", paymentId, cachedStatus);
            return cachedStatus;
        }
        
        // If not in cache, get from payment gateway
        try {
            PaymentResponseDto response = paymentGatewayClient.getPaymentStatus(paymentId);
            String status = response.getStatus();
            
            // Update cache with fetched status
            if (!"ERROR".equals(status) && !"UNKNOWN".equals(status)) {
                paymentStatusCache.put(paymentId, status);
            }
            
            return status;
        } catch (Exception e) {
            logger.error("Error retrieving payment status for ID: {}", paymentId, e);
            throw new PaymentException("Failed to retrieve payment status: " + e.getMessage(), paymentId, e);
        }
    }

    /**
     * Validates payment request data before processing
     * 
     * @param paymentRequest The request to validate
     * @throws PaymentException if validation fails
     */
    private void validatePaymentRequest(PaymentRequestDto paymentRequest) {
        if (paymentRequest == null) {
            throw new PaymentException("Payment request cannot be null");
        }
        
        if (paymentRequest.getOrderId() == null || paymentRequest.getOrderId().trim().isEmpty()) {
            throw new PaymentException("Order ID is required");
        }
        
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount must be positive", null, "INVALID_AMOUNT");
        }
        
        if (paymentRequest.getCurrency() == null || paymentRequest.getCurrency().trim().isEmpty()) {
            throw new PaymentException("Currency is required");
        }
        
        // Validate payment method and relevant details
        if ("CREDIT_CARD".equals(paymentRequest.getPaymentMethod())) {
            // FIXME: Enhance credit card validation with proper security checks
            if (paymentRequest.getCardNumber() == null || paymentRequest.getCardNumber().trim().isEmpty()) {
                throw new PaymentException("Card number is required for credit card payments");
            }
            
            // TODO: Add more validation for expiry date and CVV
        }
        
        // Additional payment method validations can be added here
    }
}