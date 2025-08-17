package com.gradlemedium200.orderservice.dto;

import com.gradlemedium200.orderservice.model.ShoppingCart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for cart responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    
    private String cartId;
    private String customerId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDto> cartItems;
    
    /**
     * Sets the cart items
     * 
     * @param items The cart items to set
     */
    public void setCartItems(List<CartItemDto> items) {
        this.cartItems = items;
    }
    
    /**
     * Sets the total amount
     * 
     * @param amount The total amount to set
     */
    public void setTotalAmount(BigDecimal amount) {
        this.totalAmount = amount;
    }
    
    /**
     * Convert from a ShoppingCart entity to a CartResponseDto.
     * 
     * @param cart The shopping cart entity
     * @return The cart response DTO
     */
    public static CartResponseDto fromEntity(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }
        
        List<CartItemDto> itemDtos = new ArrayList<>();
        if (cart.getCartItems() != null) {
            itemDtos = cart.getCartItems().stream()
                .map(CartItemDto::fromCartItem)
                .collect(Collectors.toList());
        }
        
        return CartResponseDto.builder()
            .cartId(cart.getCartId())
            .customerId(cart.getCustomerId())
            .totalAmount(cart.getTotalAmount())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .cartItems(itemDtos)
            .build();
    }
}