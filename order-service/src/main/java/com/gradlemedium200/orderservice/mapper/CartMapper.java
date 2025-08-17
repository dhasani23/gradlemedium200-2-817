package com.gradlemedium200.orderservice.mapper;

import com.gradlemedium200.orderservice.dto.CartItemDto;
import com.gradlemedium200.orderservice.dto.CartRequestDto;
import com.gradlemedium200.orderservice.dto.CartResponseDto;
import com.gradlemedium200.orderservice.model.CartItem;
import com.gradlemedium200.orderservice.model.ShoppingCart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between cart entities and DTOs.
 * This class provides methods to convert ShoppingCart entities to CartResponseDto
 * objects and CartRequestDto objects to ShoppingCart entities.
 */
@Component
public class CartMapper {

    /**
     * Converts a ShoppingCart entity to a CartResponseDto.
     *
     * @param cart The shopping cart entity to convert
     * @return A CartResponseDto containing the cart data, or null if input is null
     */
    public CartResponseDto toResponseDto(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }
        
        // Use the static fromEntity method that handles nested CartItemDto correctly
        return CartResponseDto.fromEntity(cart);
    }
    
    /**
     * Maps a list of CartItem entities to a list of CartItemDto objects.
     *
     * @param cartItems The list of cart item entities to convert
     * @return A list of CartItemDto objects
     */
    private List<CartItemDto> mapCartItems(List<CartItem> cartItems) {
        if (cartItems == null) {
            return new ArrayList<>();
        }
        
        return cartItems.stream()
                .map(CartItemDto::fromCartItem)
                .collect(Collectors.toList());
    }

    /**
     * Converts a CartRequestDto to a ShoppingCart entity.
     * If an existing cart is provided, it will update that cart.
     * Otherwise, it creates a new cart with the requested item.
     *
     * @param cartRequest The cart request DTO to convert
     * @return A ShoppingCart entity containing the cart data
     */
    public ShoppingCart toEntity(CartRequestDto cartRequest) {
        if (cartRequest == null) {
            throw new IllegalArgumentException("Cart request cannot be null");
        }
        
        // Create new shopping cart
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId(UUID.randomUUID().toString());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        
        // TODO: Set customer ID from authenticated user context
        // FIXME: Currently using a placeholder customer ID
        cart.setCustomerId("anonymous-customer");
        
        // Add the requested item to the cart
        if (cartRequest.getProductId() != null && cartRequest.getQuantity() != null) {
            CartItem item = new CartItem();
            item.setCartItemId(UUID.randomUUID().toString());
            item.setProductId(cartRequest.getProductId());
            item.setQuantity(cartRequest.getQuantity());
            
            // FIXME: Product name and price should be fetched from product catalog service
            // For now, we're setting placeholder values
            item.setProductName("Unknown Product"); 
            item.setUnitPrice(BigDecimal.ZERO);
            
            List<CartItem> items = new ArrayList<>();
            items.add(item);
            cart.setCartItems(items);
        }
        
        return cart;
    }
    
    /**
     * Updates an existing ShoppingCart entity with data from a CartRequestDto.
     * This method adds the item in the request to the existing cart.
     *
     * @param existingCart The existing shopping cart entity to update
     * @param cartRequest The cart request DTO containing the item to add
     * @return The updated ShoppingCart entity
     */
    public ShoppingCart updateEntity(ShoppingCart existingCart, CartRequestDto cartRequest) {
        if (existingCart == null) {
            return toEntity(cartRequest);
        }
        
        if (cartRequest == null || cartRequest.getProductId() == null || cartRequest.getQuantity() == null) {
            return existingCart;
        }
        
        // Create a new cart item from the request
        CartItem newItem = new CartItem();
        newItem.setCartItemId(UUID.randomUUID().toString());
        newItem.setProductId(cartRequest.getProductId());
        newItem.setQuantity(cartRequest.getQuantity());
        
        // FIXME: Product name and price should be fetched from product catalog service
        // For now, we're setting placeholder values
        newItem.setProductName("Unknown Product");
        newItem.setUnitPrice(BigDecimal.ZERO);
        
        // Add the item to the existing cart
        existingCart.addItem(newItem);
        existingCart.setUpdatedAt(LocalDateTime.now());
        
        return existingCart;
    }
    
    /**
     * Merges multiple CartRequestDto objects into a single ShoppingCart.
     * This is useful when processing batch cart updates.
     *
     * @param cartRequests List of cart request DTOs to merge
     * @return A new ShoppingCart entity containing all requested items
     */
    public ShoppingCart mergeRequests(List<CartRequestDto> cartRequests) {
        if (cartRequests == null || cartRequests.isEmpty()) {
            throw new IllegalArgumentException("Cart requests cannot be null or empty");
        }
        
        ShoppingCart cart = toEntity(cartRequests.get(0));
        
        for (int i = 1; i < cartRequests.size(); i++) {
            updateEntity(cart, cartRequests.get(i));
        }
        
        return cart;
    }
    
    // TODO: Add method to map CartRequestDto directly to CartItem
    // TODO: Add support for cart item removal operations
    // FIXME: Improve error handling for invalid cart requests
}