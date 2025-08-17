package com.gradlemedium200.orderservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gradlemedium200.orderservice.service.CartService;
import com.gradlemedium200.orderservice.dto.CartRequestDto;
import com.gradlemedium200.orderservice.dto.CartResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller that handles shopping cart operations.
 * Provides endpoints for adding items to cart, removing items, retrieving cart state,
 * clearing cart and updating item quantities.
 */
@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    
    private final CartService cartService;
    
    /**
     * Constructor for dependency injection
     * 
     * @param cartService Service handling cart business logic
     */
    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    /**
     * Adds an item to the customer's shopping cart
     * 
     * @param customerId The ID of the customer
     * @param cartRequest DTO containing item details to be added
     * @return ResponseEntity with updated cart information
     */
    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @PathVariable @NotBlank String customerId,
            @Valid @RequestBody CartRequestDto cartRequest) {
        
        logger.info("Adding item to cart for customer: {}", customerId);
        
        try {
            CartResponseDto updatedCart = cartService.addItemToCart(customerId, cartRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
        } catch (Exception e) {
            logger.error("Error adding item to cart for customer {}: {}", customerId, e.getMessage(), e);
            throw e; // Let global exception handler take care of this
        }
    }
    
    /**
     * Removes an item from the customer's shopping cart
     * 
     * @param customerId The ID of the customer
     * @param productId The ID of the product to remove
     * @return ResponseEntity with updated cart information
     */
    @DeleteMapping("/{customerId}/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(
            @PathVariable @NotBlank String customerId,
            @PathVariable @NotBlank String productId) {
        
        logger.info("Removing product {} from cart for customer: {}", productId, customerId);
        
        try {
            CartResponseDto updatedCart = cartService.removeItemFromCart(customerId, productId);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            logger.error("Error removing product {} from cart for customer {}: {}", 
                    productId, customerId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves the current state of a customer's shopping cart
     * 
     * @param customerId The ID of the customer
     * @return ResponseEntity with current cart information
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponseDto> getCart(@PathVariable @NotBlank String customerId) {
        logger.info("Retrieving cart for customer: {}", customerId);
        
        try {
            CartResponseDto cart = cartService.getCart(customerId);
            
            // If cart is empty but exists, return 200 OK with empty cart
            // If cart doesn't exist, service should handle that case
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error retrieving cart for customer {}: {}", customerId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Clears all items from a customer's shopping cart
     * 
     * @param customerId The ID of the customer
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable @NotBlank String customerId) {
        logger.info("Clearing cart for customer: {}", customerId);
        
        try {
            cartService.clearCart(customerId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error clearing cart for customer {}: {}", customerId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Updates the quantity of a specific item in a customer's shopping cart
     * 
     * @param customerId The ID of the customer
     * @param productId The ID of the product to update
     * @param quantity The new quantity for the product
     * @return ResponseEntity with updated cart information
     */
    @PatchMapping("/{customerId}/items/{productId}")
    public ResponseEntity<CartResponseDto> updateCartItemQuantity(
            @PathVariable @NotBlank String customerId,
            @PathVariable @NotBlank String productId,
            @RequestParam @Positive Integer quantity) {
        
        logger.info("Updating quantity to {} for product {} in customer {}'s cart", 
                quantity, productId, customerId);
        
        try {
            // Validation for minimum quantity
            if (quantity <= 0) {
                // FIXME: Consider using a custom exception or handling this at service layer
                return ResponseEntity.badRequest().build();
            }
            
            CartResponseDto updatedCart = cartService.updateCartItemQuantity(customerId, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            logger.error("Error updating quantity for product {} in customer {}'s cart: {}", 
                    productId, customerId, e.getMessage(), e);
            throw e;
        }
    }
    
    // TODO: Add endpoint for merging guest cart with user cart after login
    // TODO: Add endpoint for retrieving cart summary with pricing information
    // TODO: Add support for cart item notes or customization options
}