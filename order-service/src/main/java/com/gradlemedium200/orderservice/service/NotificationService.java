package com.gradlemedium200.orderservice.service;

import com.amazonaws.services.sns.model.PublishResult;
import com.gradlemedium200.orderservice.dto.NotificationDto;
import com.gradlemedium200.orderservice.integration.SnsClient;
import com.gradlemedium200.orderservice.model.OrderStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for sending order-related notifications via SNS.
 * This service handles creating and sending different types of notifications
 * related to order processing, confirmations, and updates.
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private static final String ORDER_CONFIRMATION_TYPE = "ORDER_CONFIRMATION";
    private static final String ORDER_STATUS_UPDATE_TYPE = "ORDER_STATUS_UPDATE";
    private static final String ORDER_CANCELLATION_TYPE = "ORDER_CANCELLATION";
    
    private static final Map<OrderStatus, String> STATUS_MESSAGES = new HashMap<>();
    
    static {
        STATUS_MESSAGES.put(OrderStatus.CONFIRMED, "Your order has been confirmed and is being processed.");
        STATUS_MESSAGES.put(OrderStatus.PROCESSING, "Your order is being processed and prepared for shipping.");
        STATUS_MESSAGES.put(OrderStatus.SHIPPED, "Your order has been shipped and is on its way!");
        STATUS_MESSAGES.put(OrderStatus.DELIVERED, "Your order has been delivered. Thank you for shopping with us!");
        STATUS_MESSAGES.put(OrderStatus.CANCELLED, "Your order has been cancelled.");
        STATUS_MESSAGES.put(OrderStatus.REFUNDED, "Your order has been refunded.");
    }
    
    private final SnsClient snsClient;
    
    /**
     * Constructs a notification service with the required SNS client.
     * 
     * @param snsClient client for AWS SNS integration
     */
    @Autowired
    public NotificationService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }
    
    /**
     * Sends order confirmation notification to a customer.
     * 
     * @param orderId the ID of the order
     * @param customerId the ID of the customer
     */
    public void sendOrderConfirmation(String orderId, String customerId) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        
        logger.info("Sending order confirmation notification for Order ID: {}, Customer ID: {}", orderId, customerId);
        
        String subject = "Order Confirmation: Your Order #" + orderId + " has been received";
        String message = MessageFormat.format(
            "Thank you for your order! We have received your order #{0} and it is now being processed. " +
            "We will notify you when your order ships. You can check your order status anytime by logging into your account.",
            orderId
        );
        
        NotificationDto notification = new NotificationDto(
            ORDER_CONFIRMATION_TYPE,
            subject,
            message,
            orderId,
            customerId
        );
        
        notification.addAttribute("OrderDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        sendNotification(notification);
    }
    
    /**
     * Sends order status update notification to a customer.
     * 
     * @param orderId the ID of the order
     * @param customerId the ID of the customer
     * @param newStatus the new status of the order
     */
    public void sendOrderStatusUpdate(String orderId, String customerId, OrderStatus newStatus) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(newStatus, "Order status cannot be null");
        
        logger.info("Sending order status update notification for Order ID: {}, Customer ID: {}, New Status: {}",
                orderId, customerId, newStatus);
        
        String subject = "Order Status Update: Your Order #" + orderId + " is " + newStatus;
        String message = getStatusMessage(orderId, newStatus);
        
        NotificationDto notification = new NotificationDto(
            ORDER_STATUS_UPDATE_TYPE,
            subject,
            message,
            orderId,
            customerId
        );
        
        notification.addAttribute("OrderStatus", newStatus.name());
        notification.addAttribute("StatusUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        sendNotification(notification);
    }
    
    /**
     * Sends order cancellation notification to a customer.
     * 
     * @param orderId the ID of the order
     * @param customerId the ID of the customer
     */
    public void sendOrderCancellation(String orderId, String customerId) {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        
        logger.info("Sending order cancellation notification for Order ID: {}, Customer ID: {}", orderId, customerId);
        
        String subject = "Order Cancellation: Your Order #" + orderId + " has been cancelled";
        String message = MessageFormat.format(
            "Your order #{0} has been cancelled successfully. If you have any questions about this cancellation, " +
            "please contact our customer support team.",
            orderId
        );
        
        NotificationDto notification = new NotificationDto(
            ORDER_CANCELLATION_TYPE,
            subject,
            message,
            orderId,
            customerId
        );
        
        notification.addAttribute("CancellationTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        sendNotification(notification);
    }
    
    /**
     * Sends generic notification via SNS.
     * This method can be used for sending any type of notification.
     * 
     * @param notification the notification data transfer object
     * @return the message ID from SNS
     */
    public String sendNotification(NotificationDto notification) {
        Objects.requireNonNull(notification, "Notification cannot be null");
        Objects.requireNonNull(notification.getNotificationType(), "Notification type cannot be null");
        
        try {
            // Validate notification content
            if (notification.getSubject() == null || notification.getSubject().trim().isEmpty()) {
                throw new IllegalArgumentException("Notification subject cannot be empty");
            }
            
            if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("Notification message cannot be empty");
            }
            
            // Log notification details
            logger.debug("Sending notification: Type={}, Subject={}, OrderId={}, CustomerId={}",
                    notification.getNotificationType(),
                    notification.getSubject(),
                    notification.getOrderId(),
                    notification.getCustomerId());
            
            // Send notification via SNS
            snsClient.publishNotification(notification);
            
            logger.info("Successfully sent notification");
            
            return "notification-sent"; // Return a placeholder since we don't have the actual message ID
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            
            // FIXME: Consider implementing retry logic or storing failed notifications for later retry
            
            throw new RuntimeException("Failed to send notification", e);
        }
    }
    
    /**
     * Generates an appropriate message for the given order status.
     * 
     * @param orderId the ID of the order
     * @param status the order status
     * @return a message explaining the order status
     */
    private String getStatusMessage(String orderId, OrderStatus status) {
        String statusMessage = STATUS_MESSAGES.getOrDefault(status,
                "Your order #" + orderId + " status has been updated to " + status);
                
        return MessageFormat.format(
            "Order #{0}: {1} You can check your order status anytime by logging into your account.", 
            orderId, statusMessage);
    }
    
    // TODO: Add methods for sending batch notifications
    
    // TODO: Implement notification templates to improve consistency across message types
}