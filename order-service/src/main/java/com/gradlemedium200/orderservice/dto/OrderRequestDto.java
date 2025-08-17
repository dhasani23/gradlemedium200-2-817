package com.gradlemedium200.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    
    private String customerId;
    private List<OrderItemDto> orderItems;
    private List<OrderItemDto> items;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    /**
     * Gets the order items
     * 
     * @return The order items
     */
    public List<OrderItemDto> getOrderItems() {
        return this.orderItems != null ? this.orderItems : this.items;
    }
    
    /**
     * Sets the order items
     * 
     * @param orderItems The order items
     */
    public void setOrderItems(List<OrderItemDto> orderItems) {
        this.orderItems = orderItems;
        this.items = orderItems;
    }
    
    /**
     * Sets the items (alias for order items)
     * 
     * @param items The items
     */
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
        this.orderItems = items;
    }
    private BigDecimal totalAmount;
}