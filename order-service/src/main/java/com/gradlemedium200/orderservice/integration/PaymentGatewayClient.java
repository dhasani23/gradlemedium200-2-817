package com.gradlemedium200.orderservice.integration;

import com.gradlemedium200.orderservice.dto.PaymentRequestDto;
import com.gradlemedium200.orderservice.dto.PaymentResponseDto;
import com.gradlemedium200.orderservice.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client for external payment gateway integration.
 * Handles the communication with external payment processing service.
 */
@Component
public class PaymentGatewayClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayClient.class);
    
    private final RestTemplate restTemplate;
    private final String paymentGatewayUrl;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param restTemplate REST template for HTTP calls
     * @param paymentGatewayUrl URL of the payment gateway service
     */
    @Autowired
    public PaymentGatewayClient(RestTemplate restTemplate,
                               @Value("${payment.gateway.url}") String paymentGatewayUrl) {
        this.restTemplate = restTemplate;
        this.paymentGatewayUrl = paymentGatewayUrl;
        logger.info("PaymentGatewayClient initialized with URL: {}", paymentGatewayUrl);
    }
    
    /**
     * Processes payment through external gateway.
     * 
     * @param paymentRequest Payment request data
     * @return Payment response with transaction details
     * @throws PaymentException if payment processing fails
     */
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequest) {
        logger.info("Processing payment for order: {}", paymentRequest.getOrderId());
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<PaymentRequestDto> requestEntity = new HttpEntity<>(paymentRequest, headers);
            
            ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
                    paymentGatewayUrl + "/process",
                    HttpMethod.POST,
                    requestEntity,
                    PaymentResponseDto.class);
            
            PaymentResponseDto paymentResponse = response.getBody();
            
            if (paymentResponse != null) {
                logger.info("Payment processed with ID: {}, status: {}", 
                        paymentResponse.getPaymentId(), paymentResponse.getStatus());
                
                if (!paymentResponse.isSuccessful()) {
                    logger.warn("Payment processing failed: {} - {}", 
                            paymentResponse.getErrorCode(), paymentResponse.getErrorMessage());
                }
                
                return paymentResponse;
            } else {
                throw new PaymentException("Empty response received from payment gateway");
            }
        } catch (RestClientException e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves payment status from gateway.
     * 
     * @param paymentId ID of the payment to check
     * @return Payment response with status details
     * @throws PaymentException if status retrieval fails
     */
    public PaymentResponseDto getPaymentStatus(String paymentId) {
        logger.info("Retrieving status for payment: {}", paymentId);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
                    paymentGatewayUrl + "/status/" + paymentId,
                    HttpMethod.GET,
                    requestEntity,
                    PaymentResponseDto.class);
            
            PaymentResponseDto paymentResponse = response.getBody();
            
            if (paymentResponse != null) {
                logger.info("Payment status retrieved: {}", paymentResponse.getStatus());
                return paymentResponse;
            } else {
                throw new PaymentException("Empty response received when retrieving payment status", paymentId);
            }
        } catch (RestClientException e) {
            logger.error("Error retrieving payment status: {}", e.getMessage(), e);
            throw new PaymentException("Failed to retrieve payment status: " + e.getMessage(), 
                    paymentId, e);
        }
    }
    
    /**
     * Processes payment refund.
     * 
     * @param paymentId ID of the payment to refund
     * @param amount Amount to refund
     * @return Payment response with refund details
     * @throws PaymentException if refund processing fails
     */
    public PaymentResponseDto refundPayment(String paymentId, BigDecimal amount) {
        logger.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
        
        try {
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> refundRequest = new HashMap<>();
            refundRequest.put("paymentId", paymentId);
            refundRequest.put("amount", amount);
            refundRequest.put("refundId", UUID.randomUUID().toString());
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(refundRequest, headers);
            
            ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
                    paymentGatewayUrl + "/refund",
                    HttpMethod.POST,
                    requestEntity,
                    PaymentResponseDto.class);
            
            PaymentResponseDto paymentResponse = response.getBody();
            
            if (paymentResponse != null) {
                logger.info("Refund processed with status: {}", paymentResponse.getStatus());
                return paymentResponse;
            } else {
                throw new PaymentException("Empty response received when processing refund", paymentId);
            }
        } catch (RestClientException e) {
            logger.error("Error processing refund: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process refund: " + e.getMessage(), 
                    paymentId, e);
        }
    }
    
    /**
     * Validates payment with gateway.
     * 
     * @param paymentId ID of the payment to validate
     * @return true if payment is valid, false otherwise
     * @throws PaymentException if validation fails
     */
    public boolean validatePayment(String paymentId) {
        logger.info("Validating payment: {}", paymentId);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    paymentGatewayUrl + "/validate/" + paymentId,
                    HttpMethod.GET,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> validationResult = response.getBody();
            
            if (validationResult != null && validationResult.containsKey("valid")) {
                boolean isValid = Boolean.TRUE.equals(validationResult.get("valid"));
                logger.info("Payment validation result: {}", isValid);
                return isValid;
            } else {
                logger.warn("Invalid validation response structure");
                return false;
            }
        } catch (RestClientException e) {
            logger.error("Error validating payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to validate payment: " + e.getMessage(), 
                    paymentId, e);
        }
    }
    
    /**
     * Creates standard HTTP headers for payment gateway requests.
     * 
     * @return HttpHeaders with standard values
     */
    /**
     * Process a refund for a given payment.
     * Alias for refundPayment to maintain API compatibility.
     * 
     * @param paymentId ID of the payment to refund
     * @param amount Amount to refund
     * @return Payment response with refund details
     * @throws PaymentException if refund processing fails
     */
    public PaymentResponseDto processRefund(String paymentId, BigDecimal amount) {
        return refundPayment(paymentId, amount);
    }
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // FIXME: Add authentication headers once gateway auth requirements are defined
        // headers.set("Authorization", "Bearer " + apiKey);
        
        // TODO: Add request correlation ID for better traceability
        
        return headers;
    }
}