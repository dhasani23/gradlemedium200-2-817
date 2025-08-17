package com.gradlemedium200.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for representing an order item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    /**
     * Constructor with essential fields.
     * 
     * @param productId The product ID
     * @param productName The product name
     * @param quantity The quantity
     * @param unitPrice The unit price
     */
    public OrderItemDto(String productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Constructor with essential fields for primitive int quantity.
     * 
     * @param productId The product ID
     * @param productName The product name
     * @param quantity The quantity as primitive int
     * @param unitPrice The unit price
     */
    public OrderItemDto(String productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        if (unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculates the total price based on unit price and quantity.
     * 
     * @return The calculated total price
     */
    public BigDecimal calculateTotalPrice() {
        if (this.unitPrice == null || this.quantity == null) {
            return BigDecimal.ZERO;
        }
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        return this.totalPrice;
    }
}