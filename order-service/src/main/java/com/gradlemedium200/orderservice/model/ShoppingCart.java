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
 * Represents a shopping cart for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    
    private String cartId;
    private String customerId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
    
    /**
     * Constructor with just customerId
     * 
     * @param customerId The customer ID
     */
    public ShoppingCart(String customerId) {
        this.customerId = customerId;
        this.cartId = java.util.UUID.randomUUID().toString();
        this.cartItems = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Adds an item to the cart.
     * 
     * @param item The cart item to add
     */
    public void addItem(CartItem item) {
        if (this.cartItems == null) {
            this.cartItems = new ArrayList<>();
        }
        
        // Check if the product already exists in the cart
        boolean productExists = false;
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductId().equals(item.getProductId())) {
                // Update the existing item
                Integer newQuantity = existingItem.getQuantity() != null && item.getQuantity() != null ? 
                    existingItem.getQuantity() + item.getQuantity() : 
                    (existingItem.getQuantity() != null ? existingItem.getQuantity() : 
                    (item.getQuantity() != null ? item.getQuantity() : 1));
                existingItem.setQuantity(newQuantity);
                if (existingItem.getUnitPrice() != null && existingItem.getQuantity() != null) {
                    existingItem.setTotalPrice(existingItem.getUnitPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
                }
                productExists = true;
                break;
            }
        }
        
        // If the product doesn't exist in the cart, add it
        if (!productExists) {
            this.cartItems.add(item);
        }
        
        // Update total amount and last updated timestamp
        calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Removes an item from the cart.
     * 
     * @param productId The product ID to remove
     * @return True if the item was removed, false if not found
     */
    public boolean removeItem(String productId) {
        if (this.cartItems == null) {
            return false;
        }
        
        boolean removed = cartItems.removeIf(item -> item.getProductId().equals(productId));
        
        if (removed) {
            // Update total amount and last updated timestamp
            calculateTotalAmount();
            this.updatedAt = LocalDateTime.now();
        }
        
        return removed;
    }
    
    /**
     * Updates the quantity of an item in the cart.
     * 
     * @param productId The product ID
     * @param quantity The new quantity
     * @return True if the item was updated, false if not found
     */
    public boolean updateItemQuantity(String productId, Integer quantity) {
        if (this.cartItems == null) {
            return false;
        }
        
        if (quantity == null) {
            return false;
        }
        
        boolean updated = false;
        
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                if (item.getUnitPrice() != null) {
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                }
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
     * Calculates the total amount of the cart based on all items.
     * 
     * @return The calculated total amount
     */
    public BigDecimal calculateTotalAmount() {
        if (this.cartItems == null || this.cartItems.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = this.cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = total;
        return total;
    }
    
    /**
     * Clears all items from the cart.
     */
    public void clearCart() {
        if (this.cartItems != null) {
            this.cartItems.clear();
        } else {
            this.cartItems = new ArrayList<>();
        }
        this.totalAmount = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the cart is empty.
     * 
     * @return True if the cart has no items, false otherwise
     */
    public boolean isEmpty() {
        return this.cartItems == null || this.cartItems.isEmpty();
    }
}