package com.gradlemedium200.orderservice.validator;

import com.gradlemedium200.orderservice.model.ShoppingCart;
import com.gradlemedium200.orderservice.model.CartItem;
import com.gradlemedium200.orderservice.dto.CartRequestDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validator for cart data validation.
 * This class provides methods for validating shopping cart entities and cart-related DTOs
 * to ensure data integrity and business rule compliance.
 * 
 * @author gradlemedium200
 * @version 1.0
 */
@Component
public class CartValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CartValidator.class);
    
    // Constants for validation rules
    private static final int MAX_CART_ITEMS = 100;
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 50;
    private static final BigDecimal MAX_ITEM_PRICE = new BigDecimal("10000.00");
    private static final BigDecimal MAX_CART_TOTAL = new BigDecimal("50000.00");
    
    // FIXME: Consider adding batch validation methods for multiple cart operations
    
    /**
     * Returns a list of validation errors for a shopping cart.
     * 
     * @param cart The shopping cart to validate
     * @return List of validation error messages, empty if cart is valid
     */
    public List<String> validateCart(ShoppingCart cart) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        logger.debug("Validating shopping cart: {}", cart != null ? cart.getCartId() : "null");
        
        // Check for null cart
        if (cart == null) {
            errors.add("Cart is null");
            logger.warn("Cart validation failed: cart is null");
            return errors;
        }
        
        // Check required fields
        if (isNullOrEmpty(cart.getCartId())) {
            errors.add("Cart ID is required");
            logger.warn("Cart validation failed: cart ID missing for cart");
        }
        
        if (isNullOrEmpty(cart.getCustomerId())) {
            errors.add("Customer ID is required");
            logger.warn("Cart validation failed: customer ID missing for cart {}", cart.getCartId());
        }
        
        // Allow empty cart (valid state)
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            logger.debug("Cart validation passed: empty cart for customer {}", cart.getCustomerId());
            return errors;
        }
        
        // Check cart items count
        if (cart.getCartItems().size() > MAX_CART_ITEMS) {
            errors.add("Too many items in cart (maximum " + MAX_CART_ITEMS + ")");
            logger.warn("Cart validation failed: too many cart items ({}) for cart {}", 
                      cart.getCartItems().size(), cart.getCartId());
        }
        
        // Validate each cart item
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                List<String> itemErrors = validateCartItem(item);
                if (!itemErrors.isEmpty()) {
                    errors.addAll(itemErrors);
                    logger.warn("Cart validation failed: invalid items in cart {}", cart.getCartId());
                }
            }
        }
        
        // Validate total amount
        BigDecimal calculatedTotal = cart.calculateTotalAmount();
        if (calculatedTotal.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Total amount cannot be negative");
            logger.warn("Cart validation failed: invalid total amount {} for cart {}", 
                      calculatedTotal, cart.getCartId());
        }
        
        // Validate max cart total
        if (calculatedTotal.compareTo(MAX_CART_TOTAL) > 0) {
            errors.add("Total amount exceeds maximum allowed (" + MAX_CART_TOTAL + ")");
            logger.warn("Cart validation failed: total amount {} exceeds maximum allowed {} for cart {}", 
                      calculatedTotal, MAX_CART_TOTAL, cart.getCartId());
        }
        
        // Validate timestamps
        if (cart.getCreatedAt() == null) {
            errors.add("Created timestamp is required");
            logger.warn("Cart validation failed: missing created timestamp for cart {}", cart.getCartId());
        }
        
        if (cart.getUpdatedAt() == null) {
            errors.add("Updated timestamp is required");
            logger.warn("Cart validation failed: missing updated timestamp for cart {}", cart.getCartId());
        }
        
        // Ensure updatedAt is not before createdAt
        if (cart.getCreatedAt() != null && cart.getUpdatedAt() != null && 
                cart.getUpdatedAt().isBefore(cart.getCreatedAt())) {
            errors.add("Updated timestamp cannot be before created timestamp");
            logger.warn("Cart validation failed: updatedAt before createdAt for cart {}", cart.getCartId());
        }
        
        if (errors.isEmpty()) {
            logger.debug("Cart validation passed for cart: {}", cart.getCartId());
        }
        
        return errors;
    }
    
    /**
     * Validates an individual cart item and returns a list of validation errors.
     * 
     * @param item The cart item to validate
     * @return List of validation error messages, empty if item is valid
     */
    public List<String> validateCartItem(CartItem item) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (item == null) {
            errors.add("Cart item is null");
            return errors;
        }
        
        // Check required fields
        if (isNullOrEmpty(item.getProductId())) {
            errors.add("Product ID is required");
        }
        
        if (isNullOrEmpty(item.getProductName())) {
            errors.add("Product name is required");
        }
        
        // Check item ID - can be null for new items, but if provided must not be empty
        if (item.getCartItemId() != null && item.getCartItemId().trim().isEmpty()) {
            errors.add("Cart item ID cannot be empty if provided");
        }
        
        // Check quantity
        if (item.getQuantity() == null) {
            errors.add("Quantity is required");
        } else if (item.getQuantity() < MIN_QUANTITY) {
            errors.add("Quantity must be at least " + MIN_QUANTITY);
        } else if (item.getQuantity() > MAX_QUANTITY) {
            errors.add("Quantity cannot exceed " + MAX_QUANTITY);
        }
        
        // Check prices
        if (item.getUnitPrice() == null) {
            errors.add("Unit price is required");
        } else if (item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Unit price must be greater than zero");
        } else if (item.getUnitPrice().compareTo(MAX_ITEM_PRICE) > 0) {
            errors.add("Unit price cannot exceed " + MAX_ITEM_PRICE);
        }
        
        // Verify total price calculation
        if (item.getUnitPrice() != null && item.getQuantity() != null && item.getTotalPrice() != null) {
            BigDecimal calculatedTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity().doubleValue()));
            if (item.getTotalPrice().compareTo(calculatedTotal) != 0) {
                errors.add("Total price does not match unit price multiplied by quantity");
            }
        } else if (item.getTotalPrice() == null) {
            errors.add("Total price is required");
        }
        
        return errors;
    }
    
    /**
     * Validates a cart request DTO and returns a list of validation errors.
     * 
     * @param cartRequest The cart request DTO to validate
     * @return List of validation error messages, empty if request is valid
     */
    public List<String> validateCartRequest(CartRequestDto cartRequest) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        logger.debug("Validating cart request for product: {}", 
                   cartRequest != null ? cartRequest.getProductId() : "null");
        
        // Check for null request
        if (cartRequest == null) {
            errors.add("Cart request is null");
            logger.warn("Cart request validation failed: request is null");
            return errors;
        }
        
        // Check required fields
        if (isNullOrEmpty(cartRequest.getProductId())) {
            errors.add("Product ID is required");
            logger.warn("Cart request validation failed: product ID missing");
        }
        
        // Check quantity
        if (cartRequest.getQuantity() == null) {
            errors.add("Quantity is required");
            logger.warn("Cart request validation failed: quantity missing for product {}", 
                      cartRequest.getProductId());
        } else if (cartRequest.getQuantity() < MIN_QUANTITY) {
            errors.add("Quantity must be at least " + MIN_QUANTITY);
            logger.warn("Cart request validation failed: quantity {} below minimum for product {}", 
                      cartRequest.getQuantity(), cartRequest.getProductId());
        } else if (cartRequest.getQuantity() > MAX_QUANTITY) {
            errors.add("Quantity cannot exceed " + MAX_QUANTITY);
            logger.warn("Cart request validation failed: quantity {} exceeds maximum for product {}", 
                      cartRequest.getQuantity(), cartRequest.getProductId());
        }
        
        if (errors.isEmpty()) {
            logger.debug("Cart request validation passed for product: {}", cartRequest.getProductId());
        }
        
        return errors;
    }
    
    /**
     * Validates a quantity value.
     * 
     * @param quantity The quantity to validate
     * @return List of validation error messages, empty if quantity is valid
     */
    public List<String> validateQuantity(Integer quantity) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (quantity == null) {
            errors.add("Quantity is required");
        } else if (quantity < MIN_QUANTITY) {
            errors.add("Quantity must be at least " + MIN_QUANTITY);
        } else if (quantity > MAX_QUANTITY) {
            errors.add("Quantity cannot exceed " + MAX_QUANTITY);
        }
        
        return errors;
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