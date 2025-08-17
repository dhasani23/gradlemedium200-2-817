package com.gradlemedium200.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an item in a shopping cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    private String cartItemId;
    private String cartId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    /**
     * Constructor with essential fields
     * 
     * @param cartItemId ID of the cart item
     * @param productId ID of the product
     * @param productName Name of the product
     * @param quantity Quantity of the product
     * @param unitPrice Unit price of the product
     */
    public CartItem(String cartItemId, String productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Updates the quantity and recalculates the total price
     * 
     * @param quantity The new quantity
     */
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null && this.quantity != null) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}