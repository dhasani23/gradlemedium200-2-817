package com.gradlemedium200.orderservice.dto;

import com.gradlemedium200.orderservice.model.CartItem;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) for cart item data.
 * Used for transferring cart item information between layers and in API responses/requests.
 */
public class CartItemDto {

    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    /**
     * Default constructor
     */
    public CartItemDto() {
    }

    /**
     * Parameterized constructor to create a cart item DTO with all required fields
     *
     * @param productId   identifier of the product
     * @param productName name of the product
     * @param quantity    quantity of the product in cart
     * @param unitPrice   unit price of the product
     * @param totalPrice  total price for this cart item
     */
    public CartItemDto(String productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * Creates a CartItemDto from a CartItem entity
     *
     * @param cartItem the CartItem entity to convert
     * @return a new CartItemDto populated from the CartItem entity
     */
    public static CartItemDto fromCartItem(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        CartItemDto dto = new CartItemDto();
        dto.setProductId(cartItem.getProductId());
        dto.setProductName(cartItem.getProductName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setUnitPrice(cartItem.getUnitPrice());
        dto.setTotalPrice(cartItem.getTotalPrice());
        
        return dto;
    }

    /**
     * Converts this DTO to a CartItem entity
     *
     * @return a new CartItem entity populated from this DTO
     */
    public CartItem toCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setProductId(this.productId);
        cartItem.setProductName(this.productName);
        cartItem.setQuantity(this.quantity);
        cartItem.setUnitPrice(this.unitPrice);
        // Note: cartItemId will be null here, typically generated or set elsewhere
        
        return cartItem;
    }

    /**
     * Recalculates the total price based on quantity and unit price.
     * This ensures the total price is always in sync with quantity and unit price.
     */
    public void recalculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    /**
     * Get the product ID
     *
     * @return the product identifier
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Set the product ID
     *
     * @param productId the product identifier to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get the product name
     *
     * @return the name of the product
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Set the product name
     *
     * @param productName the product name to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Get the quantity
     *
     * @return the quantity of the product in the cart
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity and recalculate the total price
     *
     * @param quantity the quantity to set
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateTotalPrice();
    }

    /**
     * Get the unit price
     *
     * @return the unit price of the product
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Set the unit price and recalculate the total price
     *
     * @param unitPrice the unit price to set
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateTotalPrice();
    }

    /**
     * Get the total price
     *
     * @return the total price for this cart item
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Set the total price directly
     * Note: This is generally not recommended as total price should be calculated
     * from quantity and unit price, but may be useful for deserialization.
     *
     * @param totalPrice the total price to set
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDto that = (CartItemDto) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(totalPrice, that.totalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, quantity, unitPrice, totalPrice);
    }

    @Override
    public String toString() {
        return "CartItemDto{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
    
    // TODO: Add validation annotations for API request validation
    // TODO: Consider adding additional helper methods for cart operations
    // FIXME: Ensure proper handling of null values in calculations
}