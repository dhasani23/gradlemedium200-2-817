package com.gradlemedium200.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    private String id;
    private String orderId;
    private String userId;
    private String customerId;
    private String status;
    private OrderStatus orderStatus; // Using OrderStatus enum instead of String
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    /**
     * Adds an item to the order.
     * 
     * @param item The order item to add
     */
    public void addOrderItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        
        this.items.add(item);
        
        // Update total amount and last updated timestamp
        calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Removes an item from the order.
     * 
     * @param productId The product ID to remove
     * @return True if the item was removed, false if not found
     */
    public boolean removeOrderItem(String productId) {
        if (this.items == null) {
            return false;
        }
        
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        
        if (removed) {
            // Update total amount and last updated timestamp
            calculateTotalAmount();
            this.updatedAt = LocalDateTime.now();
        }
        
        return removed;
    }
    
    /**
     * Updates the quantity of an item in the order.
     * 
     * @param productId The product ID
     * @param quantity The new quantity
     * @return True if the item was updated, false if not found
     */
    public boolean updateItemQuantity(String productId, Integer quantity) {
        if (this.items == null) {
            return false;
        }
        
        if (quantity == null) {
            return false;
        }
        
        boolean updated = false;
        
        for (OrderItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                updated = true;
                break;
            }
        }
        
        if (updated) {
            // Update total amount and last updated timestamp
            calculateTotalAmount();
            this.updatedAt = LocalDateTime.now();
        }
        
        return updated;
    }
    
    /**
     * Calculates the total amount of the order based on all items.
     * 
     * @return The calculated total amount
     */
    public BigDecimal calculateTotalAmount() {
        if ((this.items == null || this.items.isEmpty()) && 
            (this.orderItems == null || this.orderItems.isEmpty())) {
            this.totalAmount = BigDecimal.ZERO;
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        
        // Calculate from items list
        if (this.items != null) {
            total = total.add(this.items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        
        // Calculate from orderItems list (for backward compatibility)
        if (this.orderItems != null) {
            total = total.add(this.orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        
        this.totalAmount = total;
        return total;
    }
}