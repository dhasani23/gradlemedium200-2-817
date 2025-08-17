package com.gradlemedium200.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradlemedium200.orderservice.dto.OrderEventDto;
import com.gradlemedium200.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler for processing SQS messages for asynchronous order processing.
 * This component receives order events from SQS and routes them to the appropriate
 * processing methods based on the event type.
 */
@Component
public class SqsMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SqsMessageHandler.class);
    
    // Event type constants
    public static final String ORDER_CREATED = "CREATED";
    public static final String ORDER_STATUS_UPDATED = "UPDATED";
    public static final String ORDER_CANCELLED = "CANCELLED";
    public static final String PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new SqsMessageHandler with required dependencies.
     *
     * @param orderService  Order service for processing order events
     * @param objectMapper  JSON object mapper for message parsing
     */
    @Autowired
    public SqsMessageHandler(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /**
     * Handles incoming order event messages from SQS.
     * This method serves as the main entry point for SQS message processing.
     *
     * @param message The SQS message body as a string
     */
    public void handleOrderEvent(String message) {
        logger.info("Received SQS message for processing");
        
        if (message == null || message.isEmpty()) {
            logger.warn("Received empty or null message, ignoring");
            return;
        }

        try {
            OrderEventDto orderEvent = parseMessage(message);
            
            if (orderEvent == null) {
                logger.error("Failed to parse order event from message");
                return;
            }
            
            logger.info("Processing order event of type: {}, for order ID: {}", 
                    orderEvent.getEventType(), orderEvent.getOrderId());
                    
            // Route to appropriate handler based on event type
            switch (orderEvent.getEventType()) {
                case ORDER_CREATED:
                    processOrderCreation(orderEvent);
                    break;
                    
                case ORDER_STATUS_UPDATED:
                    processOrderStatusUpdate(orderEvent);
                    break;
                    
                case ORDER_CANCELLED:
                    // FIXME: Implement order cancellation handler
                    logger.warn("Order cancellation handling not yet implemented");
                    break;
                    
                case PAYMENT_CONFIRMED:
                    // TODO: Implement payment confirmation handler
                    logger.info("Payment confirmed for order: {}", orderEvent.getOrderId());
                    break;
                    
                default:
                    logger.warn("Unknown event type: {}", orderEvent.getEventType());
            }
            
        } catch (Exception e) {
            logger.error("Error processing SQS message: {}", e.getMessage(), e);
            // TODO: Implement dead letter queue logic for failed messages
        }
    }

    /**
     * Processes order creation events.
     * Triggers asynchronous order processing after order creation.
     *
     * @param orderEvent The order event data
     */
    public void processOrderCreation(OrderEventDto orderEvent) {
        logger.info("Processing order creation event for order: {}", orderEvent.getOrderId());
        
        try {
            // Validate the event data
            if (orderEvent.getOrderId() == null || orderEvent.getOrderId().isEmpty()) {
                logger.error("Invalid order creation event: missing order ID");
                return;
            }
            
            // Process the order through the order service
            orderService.processOrder(orderEvent.getOrderId());
            
            logger.info("Successfully processed order creation for order: {}", orderEvent.getOrderId());
            
        } catch (Exception e) {
            logger.error("Failed to process order creation event: {}", e.getMessage(), e);
            // TODO: Implement retry logic or manual intervention notification
        }
    }

    /**
     * Processes order status update events.
     * Updates order status based on external system events.
     *
     * @param orderEvent The order event data
     */
    public void processOrderStatusUpdate(OrderEventDto orderEvent) {
        logger.info("Processing order status update event for order: {}", orderEvent.getOrderId());
        
        try {
            // Validate the event data
            if (orderEvent.getOrderId() == null || orderEvent.getOrderId().isEmpty() || 
                orderEvent.getEventData() == null || !orderEvent.getEventData().containsKey("newStatus")) {
                logger.error("Invalid order status update event: missing order ID or status");
                return;
            }
            
            // Get the new status from event data
            String newStatus = orderEvent.getEventData().get("newStatus").toString();
            
            // Update the order status
            orderService.updateOrderStatus(orderEvent.getOrderId(), newStatus);
            
            logger.info("Successfully updated status for order: {} to {}", 
                    orderEvent.getOrderId(), newStatus);
                    
        } catch (Exception e) {
            logger.error("Failed to process order status update event: {}", e.getMessage(), e);
            // TODO: Implement alert mechanism for failed status updates
        }
    }

    /**
     * Parses SQS message to order event DTO.
     * Converts JSON message string to OrderEventDto object.
     *
     * @param message The SQS message body as a string
     * @return The parsed OrderEventDto object
     * @throws JsonProcessingException if message parsing fails
     */
    public OrderEventDto parseMessage(String message) throws JsonProcessingException {
        try {
            return objectMapper.readValue(message, OrderEventDto.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse message to OrderEventDto: {}", e.getMessage());
            throw e;
        }
    }
}