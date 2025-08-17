package com.gradlemedium200.orderservice.validator;

import com.gradlemedium200.orderservice.model.Order;
import com.gradlemedium200.orderservice.model.OrderItem;
import com.gradlemedium200.orderservice.model.OrderStatus;
import com.gradlemedium200.orderservice.dto.OrderRequestDto;
import com.gradlemedium200.orderservice.dto.OrderItemDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validator for order data validation and business rules.
 * This class provides methods for validating order entities and DTOs
 * to ensure data integrity and business rule compliance.
 * 
 * @author gradlemedium200
 * @version 1.0
 */
public class OrderValidator {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidator.class);
    
    // Constants for validation rules
    private static final int MAX_ORDER_ITEMS = 50;
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;
    private static final int MAX_ADDRESS_LENGTH = 500;
    private static final BigDecimal MAX_ITEM_PRICE = new BigDecimal("10000.00");
    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("50000.00");

    /**
     * Validates order entity data for completeness and correctness.
     * 
     * @param order The order entity to validate
     * @return true if the order is valid, false otherwise
     */
    public boolean validateOrder(Order order) {
        logger.debug("Validating order: {}", order != null ? order.getOrderId() : "null");
        
        // Check for null order
        if (order == null) {
            logger.warn("Order validation failed: order is null");
            return false;
        }
        
        // Check required fields
        if (isNullOrEmpty(order.getOrderId()) || 
            isNullOrEmpty(order.getCustomerId()) || 
            isNullOrEmpty(order.getShippingAddress())) {
            logger.warn("Order validation failed: required fields missing for order {}", order.getOrderId());
            return false;
        }
        
        // Validate shipping address length
        if (order.getShippingAddress().length() > MAX_ADDRESS_LENGTH) {
            logger.warn("Order validation failed: shipping address too long for order {}", order.getOrderId());
            return false;
        }
        
        // Check order items
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            logger.warn("Order validation failed: no order items for order {}", order.getOrderId());
            return false;
        }
        
        // Check order items count
        if (order.getOrderItems().size() > MAX_ORDER_ITEMS) {
            logger.warn("Order validation failed: too many order items ({}) for order {}", 
                        order.getOrderItems().size(), order.getOrderId());
            return false;
        }
        
        // Validate each order item
        for (OrderItem item : order.getOrderItems()) {
            if (!validateOrderItem(item)) {
                logger.warn("Order validation failed: invalid order item for order {}", order.getOrderId());
                return false;
            }
        }
        
        // Validate order status
        if (order.getOrderStatus() == null) {
            logger.warn("Order validation failed: order status is null for order {}", order.getOrderId());
            return false;
        }
        
        // Validate total amount
        BigDecimal calculatedTotal = order.calculateTotalAmount();
        if (calculatedTotal.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Order validation failed: invalid total amount {} for order {}", 
                        calculatedTotal, order.getOrderId());
            return false;
        }
        
        // Validate max order total
        if (calculatedTotal.compareTo(MAX_ORDER_TOTAL) > 0) {
            logger.warn("Order validation failed: total amount {} exceeds maximum allowed {} for order {}", 
                        calculatedTotal, MAX_ORDER_TOTAL, order.getOrderId());
            return false;
        }
        
        logger.debug("Order validation passed for order: {}", order.getOrderId());
        return true;
    }
    
    /**
     * Validates an individual order item.
     * 
     * @param item The order item to validate
     * @return true if the item is valid, false otherwise
     */
    private boolean validateOrderItem(OrderItem item) {
        if (item == null) {
            return false;
        }
        
        // Check required fields
        if (isNullOrEmpty(item.getProductId()) || isNullOrEmpty(item.getProductName())) {
            return false;
        }
        
        // Check quantity
        if (item.getQuantity() == null || item.getQuantity() < MIN_QUANTITY || item.getQuantity() > MAX_QUANTITY) {
            return false;
        }
        
        // Check prices
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0 || 
            item.getUnitPrice().compareTo(MAX_ITEM_PRICE) > 0) {
            return false;
        }
        
        if (item.getTotalPrice() == null || item.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Verify total price calculation
        BigDecimal expectedTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
        if (item.getTotalPrice().compareTo(expectedTotal) != 0) {
            // FIXME: There's a precision issue with BigDecimal calculations
            // Consider using a tolerance value or rounding to a specific precision
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates order request DTO for creating new orders.
     * 
     * @param orderRequest The order request DTO to validate
     * @return true if the order request is valid, false otherwise
     */
    public boolean validateOrderRequest(OrderRequestDto orderRequest) {
        logger.debug("Validating order request for customer: {}", 
                    orderRequest != null ? orderRequest.getCustomerId() : "null");
        
        // Check for null request
        if (orderRequest == null) {
            logger.warn("Order request validation failed: request is null");
            return false;
        }
        
        // Check required fields
        if (isNullOrEmpty(orderRequest.getCustomerId()) || 
            isNullOrEmpty(orderRequest.getShippingAddress()) ||
            isNullOrEmpty(orderRequest.getPaymentMethod())) {
            logger.warn("Order request validation failed: required fields missing for customer {}", 
                      orderRequest.getCustomerId());
            return false;
        }
        
        // Validate shipping address length
        if (orderRequest.getShippingAddress().length() > MAX_ADDRESS_LENGTH) {
            logger.warn("Order request validation failed: shipping address too long for customer {}", 
                      orderRequest.getCustomerId());
            return false;
        }
        
        // Validate order items
        return validateOrderItems(orderRequest.getOrderItems());
    }
    
    /**
     * Validates a list of order items from a DTO.
     * 
     * @param orderItems List of order item DTOs to validate
     * @return true if all items are valid, false otherwise
     */
    public boolean validateOrderItems(List<OrderItemDto> orderItems) {
        logger.debug("Validating {} order items", orderItems != null ? orderItems.size() : "null");
        
        // Check for null or empty list
        if (orderItems == null || orderItems.isEmpty()) {
            logger.warn("Order items validation failed: items list is null or empty");
            return false;
        }
        
        // Check item count
        if (orderItems.size() > MAX_ORDER_ITEMS) {
            logger.warn("Order items validation failed: too many items ({})", orderItems.size());
            return false;
        }
        
        // Track total order amount
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        
        // Validate each item
        for (OrderItemDto item : orderItems) {
            if (item == null) {
                logger.warn("Order items validation failed: item is null");
                return false;
            }
            
            // Check required fields
            if (isNullOrEmpty(item.getProductId()) || isNullOrEmpty(item.getProductName())) {
                logger.warn("Order items validation failed: required fields missing for product {}", 
                          item.getProductId());
                return false;
            }
            
            // Check quantity
            if (item.getQuantity() == null || item.getQuantity() < MIN_QUANTITY || item.getQuantity() > MAX_QUANTITY) {
                logger.warn("Order items validation failed: invalid quantity {} for product {}", 
                          item.getQuantity(), item.getProductId());
                return false;
            }
            
            // Check prices
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0 || 
                item.getUnitPrice().compareTo(MAX_ITEM_PRICE) > 0) {
                logger.warn("Order items validation failed: invalid unit price {} for product {}", 
                          item.getUnitPrice(), item.getProductId());
                return false;
            }
            
            // Calculate and validate item total
            BigDecimal itemTotal = item.calculateTotalPrice();
            if (itemTotal.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Order items validation failed: invalid total price calculation for product {}", 
                          item.getProductId());
                return false;
            }
            
            // Add to running total
            totalOrderAmount = totalOrderAmount.add(itemTotal);
        }
        
        // Validate max order total
        if (totalOrderAmount.compareTo(MAX_ORDER_TOTAL) > 0) {
            logger.warn("Order items validation failed: total amount {} exceeds maximum allowed {}", 
                      totalOrderAmount, MAX_ORDER_TOTAL);
            return false;
        }
        
        logger.debug("Order items validation passed for {} items", orderItems.size());
        return true;
    }
    
    /**
     * Validates business rules for an order entity.
     * Business rules include:
     * - Status transition validity
     * - Inventory availability (delegated to inventory service)
     * - Order date constraints
     * - Customer-specific rules
     *
     * @param order The order entity to validate against business rules
     * @return true if the order complies with all business rules, false otherwise
     */
    public boolean validateBusinessRules(Order order) {
        logger.debug("Validating business rules for order: {}", order != null ? order.getOrderId() : "null");
        
        // Check for null order
        if (order == null) {
            logger.warn("Business rules validation failed: order is null");
            return false;
        }
        
        // Validate base order data first
        if (!validateOrder(order)) {
            logger.warn("Business rules validation failed: invalid order data for order {}", order.getOrderId());
            return false;
        }
        
        // Validate status transitions if order is being updated
        // This is just a basic example; real implementation would depend on the current and target status
        if (order.getOrderId() != null && order.getOrderStatus() != OrderStatus.PENDING) {
            // Example: Cannot transition to DELIVERED directly from PENDING
            // Real implementation would use the canTransitionTo method from OrderStatus
            if (order.getOrderStatus() == OrderStatus.DELIVERED && 
                !OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)) {
                logger.warn("Business rules validation failed: invalid status transition for order {}", 
                          order.getOrderId());
                return false;
            }
            
            // Example: Cannot cancel an order that's already been delivered
            if (order.getOrderStatus() == OrderStatus.CANCELLED && 
                (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.SHIPPED)) {
                logger.warn("Business rules validation failed: cannot cancel order {} in status {}", 
                          order.getOrderId(), order.getOrderStatus());
                return false;
            }
        }
        
        // TODO: Add validation for inventory availability (requires inventory service)
        
        // TODO: Add validation for customer-specific rules (spending limits, etc.)
        
        logger.debug("Business rules validation passed for order: {}", order.getOrderId());
        return true;
    }
    
    /**
     * Helper method to check if a string is null or empty.
     * 
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}