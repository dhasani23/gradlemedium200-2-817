package com.gradlemedium200.orderservice.service;

import com.gradlemedium200.orderservice.dto.CartRequestDto;
import com.gradlemedium200.orderservice.dto.CartResponseDto;
import com.gradlemedium200.orderservice.dto.OrderItemDto;
import com.gradlemedium200.orderservice.dto.OrderRequestDto;
import com.gradlemedium200.orderservice.dto.ProductDto;
import com.gradlemedium200.orderservice.mapper.CartMapper;
import com.gradlemedium200.orderservice.exception.CartException;
import com.gradlemedium200.orderservice.model.CartItem;
import com.gradlemedium200.orderservice.model.ShoppingCart;
import com.gradlemedium200.orderservice.repository.CartRepository;
import com.gradlemedium200.orderservice.validator.CartValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic service for shopping cart operations and cart-to-order conversion.
 * Handles all shopping cart related functionality including adding, removing, updating items,
 * and converting cart contents to an order.
 */
@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ProductValidationService productValidationService;
    private final CartValidator cartValidator;
    private final CartMapper cartMapper;

    /**
     * Constructor for dependency injection
     *
     * @param cartRepository Repository for cart data persistence
     * @param productValidationService Service for product validation
     * @param cartValidator Validator for cart data
     * @param cartMapper Mapper for converting between cart entities and DTOs
     */
    @Autowired
    public CartService(CartRepository cartRepository, 
                      ProductValidationService productValidationService,
                      CartValidator cartValidator,
                      CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.productValidationService = productValidationService;
        this.cartValidator = cartValidator;
        this.cartMapper = cartMapper;
    }

    /**
     * Adds an item to customer's shopping cart
     *
     * @param customerId Customer ID
     * @param cartRequest Cart request with item details
     * @return Updated shopping cart response
     * @throws CartException if validation fails or product is invalid
     */
    public CartResponseDto addItemToCart(String customerId, CartRequestDto cartRequest) {
        logger.info("Adding item to cart for customer: {}", customerId);
        
        // Validate inputs
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        List<String> validationErrors = cartValidator.validateCartRequest(cartRequest);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join(", ", validationErrors);
            logger.error("Cart request validation failed: {}", errorMessage);
            throw new CartException(errorMessage);
        }
        
        // Validate product exists and is active
        if (!productValidationService.isProductValid(cartRequest.getProductId())) {
            logger.error("Invalid product ID: {}", cartRequest.getProductId());
            throw new CartException("Product is not valid or no longer available");
        }
        
        // Validate product quantity
        if (!productValidationService.isQuantityAvailable(
                cartRequest.getProductId(), cartRequest.getQuantity())) {
            logger.error("Insufficient quantity available for product: {}", cartRequest.getProductId());
            throw new CartException("Requested quantity not available");
        }
        
        // Get current price to ensure price is up-to-date
        BigDecimal currentPrice = productValidationService.getCurrentPrice(cartRequest.getProductId());
        if (currentPrice != null && !currentPrice.equals(cartRequest.getUnitPrice())) {
            logger.warn("Price discrepancy for product {}. Client: {}, Actual: {}", 
                    cartRequest.getProductId(), cartRequest.getUnitPrice(), currentPrice);
            // Update to current price
            cartRequest.setUnitPrice(currentPrice);
        }
        
        // Get or create shopping cart
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElse(new ShoppingCart(customerId));
        
        // Create and add item to cart
        CartItem cartItem = new CartItem(
                UUID.randomUUID().toString(),
                cartRequest.getProductId(),
                cartRequest.getProductName(),
                cartRequest.getQuantity(),
                cartRequest.getUnitPrice()
        );
        
        cart.addItem(cartItem);
        
        // Validate the entire cart after adding the new item
        validationErrors = cartValidator.validateCart(cart);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join(", ", validationErrors);
            logger.error("Cart validation failed after adding item: {}", errorMessage);
            throw new CartException(errorMessage);
        }
        
        // Save updated cart
        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponseDto(savedCart);
    }

    /**
     * Removes an item from customer's cart
     *
     * @param customerId Customer ID
     * @param productId Product ID to remove
     * @return Updated shopping cart response
     * @throws CartException if cart or item not found
     */
    public CartResponseDto removeItemFromCart(String customerId, String productId) {
        logger.info("Removing product {} from cart for customer: {}", productId, customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        if (productId == null || productId.trim().isEmpty()) {
            throw new CartException("Product ID is required");
        }
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
        
        // Check if product exists in cart before attempting removal
        boolean productExists = cart.getCartItems().stream()
                .anyMatch(item -> productId.equals(item.getProductId()));
        
        if (!productExists) {
            throw new CartException("Product not found in cart: " + productId);
        }
        
        cart.removeItem(productId);
        cart.setUpdatedAt(LocalDateTime.now());
        
        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponseDto(savedCart);
    }

    /**
     * Retrieves a cart by its ID
     *
     * @param cartId Cart ID
     * @return Cart response DTO with the given ID
     * @throws CartException if cart not found
     */
    public CartResponseDto getCart(String cartId) {
        logger.info("Retrieving cart with ID: {}", cartId);
        
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new CartException("Cart ID is required");
        }
        
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartException("Cart not found with ID: " + cartId));
                
        return cartMapper.toResponseDto(cart);
    }
    
    /**
     * Retrieves customer's shopping cart
     *
     * @param customerId Customer ID
     * @return Cart response DTO for the customer
     * @throws CartException if cart not found
     */
    public CartResponseDto getCartByCustomerId(String customerId) {
        logger.info("Retrieving cart for customer: {}", customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
                
        return cartMapper.toResponseDto(cart);
    }

    /**
     * Clears all items from customer's cart
     *
     * @param customerId Customer ID
     * @throws CartException if cart not found
     */
    public void clearCart(String customerId) {
        logger.info("Clearing cart for customer: {}", customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
        
        cart.clearCart();
        cartRepository.save(cart);
    }

    /**
     * Updates quantity of an item in cart
     *
     * @param customerId Customer ID
     * @param productId Product ID to update
     * @param quantity New quantity
     * @return Updated shopping cart response DTO
     * @throws CartException if validation fails or item not found
     */
    public CartResponseDto updateCartItemQuantity(String customerId, String productId, Integer quantity) {
        logger.info("Updating quantity for product {} in cart for customer: {}", productId, customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        if (productId == null || productId.trim().isEmpty()) {
            throw new CartException("Product ID is required");
        }
        
        List<String> quantityValidationErrors = cartValidator.validateQuantity(quantity);
        if (!quantityValidationErrors.isEmpty()) {
            String errorMessage = String.join(", ", quantityValidationErrors);
            logger.error("Quantity validation failed: {}", errorMessage);
            throw new CartException(errorMessage);
        }
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
        
        // Find the item and update quantity
        boolean itemFound = false;
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(productId)) {
                // Validate quantity availability
                if (!productValidationService.isQuantityAvailable(productId, quantity)) {
                    throw new CartException("Requested quantity not available");
                }
                
                item.updateQuantity(quantity);
                itemFound = true;
                break;
            }
        }
        
        if (!itemFound) {
            throw new CartException("Product not found in cart: " + productId);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        
        // Validate the entire cart after updating the quantity
        List<String> cartValidationErrors = cartValidator.validateCart(cart);
        if (!cartValidationErrors.isEmpty()) {
            String errorMessage = String.join(", ", cartValidationErrors);
            logger.error("Cart validation failed after updating quantity: {}", errorMessage);
            throw new CartException(errorMessage);
        }
        
        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponseDto(savedCart);
    }

    /**
     * Converts shopping cart to order request
     *
     * @param customerId Customer ID
     * @return Order request DTO created from cart contents
     * @throws CartException if cart is empty or validation fails
     */
    public OrderRequestDto convertCartToOrder(String customerId) {
        logger.info("Converting cart to order for customer: {}", customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CartException("Customer ID is required");
        }
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
        
        if (cart.isEmpty()) {
            throw new CartException("Cannot create order from empty cart");
        }
        
        // Validate all products and quantities before converting to order
        for (CartItem item : cart.getCartItems()) {
            if (!productValidationService.isProductValid(item.getProductId())) {
                throw new CartException("Product is no longer available: " + item.getProductId());
            }
            
            if (!productValidationService.isQuantityAvailable(item.getProductId(), item.getQuantity())) {
                throw new CartException("Requested quantity not available for product: " + item.getProductId());
            }
            
            // Verify current price matches cart price
            BigDecimal currentPrice = productValidationService.getCurrentPrice(item.getProductId());
            if (currentPrice != null && !currentPrice.equals(item.getUnitPrice())) {
                logger.warn("Price changed for product {}. Cart: {}, Current: {}", 
                        item.getProductId(), item.getUnitPrice(), currentPrice);
                // FIXME: Decide whether to fail or automatically update prices
                // For now, we'll fail to ensure customer is aware of price changes
                throw new CartException("Price has changed for product: " + item.getProductName() + 
                        ". Please review your cart before ordering.");
            }
        }
        
        // Convert cart items to order items
        List<OrderItemDto> orderItems = cart.getCartItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .collect(Collectors.toList());
        
        // Calculate total amount
        BigDecimal totalAmount = cart.calculateTotalAmount();
        
        // Create order request
        OrderRequestDto orderRequest = new OrderRequestDto();
        orderRequest.setCustomerId(customerId);
        orderRequest.setItems(orderItems);
        orderRequest.setTotalAmount(totalAmount);
        
        // TODO: Add customer shipping and billing addresses from customer profile
        // TODO: Add default payment method from customer profile
        
        return orderRequest;
    }
    
    /**
     * Refreshes product information in the cart to ensure up-to-date data
     * 
     * @param customerId Customer ID
     * @return Updated shopping cart response DTO with refreshed product data
     * @throws CartException if cart not found
     */
    public CartResponseDto refreshCartProductInfo(String customerId) {
        logger.info("Refreshing product information in cart for customer: {}", customerId);
        
        ShoppingCart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart not found for customer: " + customerId));
        
        if (cart.isEmpty()) {
            return cartMapper.toResponseDto(cart);
        }
        
        boolean cartUpdated = false;
        List<CartItem> itemsToRemove = new ArrayList<>();
        
        for (CartItem item : cart.getCartItems()) {
            Optional<ProductDto> productInfo = productValidationService.getProductInfo(item.getProductId());
            
            if (productInfo.isPresent()) {
                ProductDto product = productInfo.get();
                
                if (!product.isActive()) {
                    // Mark for removal if product is no longer active
                    itemsToRemove.add(item);
                    cartUpdated = true;
                    continue;
                }
                
                // Update product name and price if changed
                if (!product.getName().equals(item.getProductName()) || 
                        !product.getPrice().equals(item.getUnitPrice())) {
                    item.setProductName(product.getName());
                    item.setUnitPrice(product.getPrice());
                    cartUpdated = true;
                }
                
                // Adjust quantity if it exceeds available inventory
                if (product.getInventoryCount() != null && 
                    item.getQuantity() > product.getInventoryCount()) {
                    item.updateQuantity(product.getInventoryCount());
                    cartUpdated = true;
                }
            } else {
                // Product no longer exists, mark for removal
                itemsToRemove.add(item);
                cartUpdated = true;
            }
        }
        
        // Remove items that are no longer available
        for (CartItem itemToRemove : itemsToRemove) {
            cart.removeItem(itemToRemove.getProductId());
        }
        
        if (cartUpdated) {
            cart.setUpdatedAt(LocalDateTime.now());
            cart = cartRepository.save(cart);
        }
        
        return cartMapper.toResponseDto(cart);
    }
}